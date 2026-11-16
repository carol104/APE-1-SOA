const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { join } = require('node:path');
const vm = require('node:vm');

function pantalla() {
    const codigo = readFileSync(join(__dirname, '../apefront/src/main/webapp/index.jsp'), 'utf8')
        .match(/<script>([\s\S]*?)<\/script>/)[1];
    const marca = { value: '1' };
    const modelo = {
        disabled: false,
        options: [],
        set innerHTML(value) { this.options = []; },
        appendChild(option) { this.options.push(option); }
    };
    const peticiones = [];
    const contexto = vm.createContext({
        document: {
            getElementById(id) { return id === 'marca' ? marca : modelo; },
            createElement() { return {}; }
        },
        fetch(url) { return new Promise(resolve => peticiones.push({ url, resolve })); },
        console: { log() {} },
        alert() {},
    });
    vm.runInContext(codigo, contexto);
    return { marca, modelo, peticiones, contexto };
}

test('cargarModelos permite esperar las opciones y conserva el modelo seleccionado', async () => {
    const { modelo, peticiones, contexto } = pantalla();
    const carga = contexto.cargarModelos('marca', 'modelo', 2);
    assert.equal(typeof carga?.then, 'function');
    assert.equal(modelo.disabled, true);
    peticiones[0].resolve({ ok: true, json: async () => [{ id: 2, modelo: 'A8' }] });
    await carga;
    assert.equal(modelo.disabled, false);
    assert.equal(modelo.options[1].value, 2);
    assert.equal(modelo.options[1].selected, true);
});

test('una respuesta de una marca anterior no reemplaza los modelos actuales', async () => {
    const { marca, modelo, peticiones, contexto } = pantalla();
    const anterior = contexto.cargarModelos('marca', 'modelo');
    marca.value = '2';
    const actual = contexto.cargarModelos('marca', 'modelo');
    peticiones[1].resolve({ ok: true, json: async () => [{ id: 20, modelo: 'Nuevo' }] });
    await actual;
    peticiones[0].resolve({ ok: true, json: async () => [{ id: 10, modelo: 'Anterior' }] });
    await anterior;
    assert.equal(modelo.options[1].value, 20);
});

test('un fallo HTTP no deja el selector bloqueado', async () => {
    const { modelo, peticiones, contexto } = pantalla();
    const carga = contexto.cargarModelos('marca', 'modelo');
    peticiones[0].resolve({ ok: false, status: 502 });
    await carga;
    assert.equal(modelo.disabled, false);
    assert.equal(modelo.options.length, 0);
});
