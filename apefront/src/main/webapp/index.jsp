<%-- 
    Document   : index
    Created on : 26 nov. 2025
--%>

<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    // Evita bucles: Solo redirige si no hay datos y no venimos del servlet
    if(request.getAttribute("vehiculos") == null){
        response.sendRedirect("SvVehiculo");
        return;
    }
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>

<%!
    private String html(Object valor) {
        if (valor == null) return "";
        return valor.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Gestión de Vehículos</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: #f0f2f5;
                margin: 0;
                padding: 20px;
            }
            table {
                font-size: 18px;
                margin-bottom: 20px;
                border-collapse: collapse;
                width: 100%;
                background: white;
            }
            th {
                background-color: #19014d;
                color: white;
                padding: 10px;
                text-align: left;
            }
            td {
                padding: 10px;
                border-bottom: 1px solid #ddd;
            }

            /* Estilos del Modal */
            .modal-bg {
                display: none;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                justify-content: center;
                align-items: center;
                z-index: 100;
            }
            .modal {
                background: white;
                padding: 25px;
                width: 400px;
                border-radius: 10px;
                box-shadow: 0px 5px 15px rgba(0,0,0,0.3);
            }
            .modal h2 {
                margin-top: 0;
                text-align: center;
                color: #19014d;
            }
            .modal input, .modal select {
                width: 100%;
                padding: 10px;
                margin-top: 10px;
                border-radius: 5px;
                border: 1px solid #ccc;
                box-sizing: border-box;
            }
            .modal input[type="submit"] {
                background: #19014d;
                color: white;
                font-weight: bold;
                cursor: pointer;
                border: none;
                margin-top: 20px;
            }
            .modal input[type="submit"]:hover {
                background: #2a027e;
            }

            /* Botones */
            .btn-open {
                background: #19014d;
                color: white;
                padding: 8px 15px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                margin-right: 5px;
            }
            .btn-danger {
                background: #dc3545;
            }
            .close-btn {
                background: #dc3545;
                color: white;
                padding: 5px 10px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                float: right;
            }
        </style>
    </head>
    <body>

        <h2>Listado de Vehículos</h2>
        <% if (request.getAttribute("errorApi") != null) { %>
        <p role="alert"><%= html(request.getAttribute("errorApi")) %></p>
        <% } %>
        <button class="btn-open" onclick="abrirInsertar()">+ Nuevo Vehículo</button>

        <div style="margin: 20px 0;">
            <input type="text" id="placaBuscar" placeholder="Buscar por placa" style="padding: 8px; border: 1px solid #ccc; border-radius: 5px;">
            <button class="btn-open" onclick="buscarPorPlaca()">Buscar</button>
            <button class="btn-open" onclick="window.location.href = 'SvVehiculo'">Ver Todos</button>
        </div>
        <br>

        <%
            String jsonVehiculos = (String) request.getAttribute("vehiculos");
            JSONArray vehiculos = (jsonVehiculos != null && !jsonVehiculos.isEmpty()) ? new JSONArray(jsonVehiculos) : new JSONArray();
        %>

        <table border="0">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Placa</th>
                    <th>Chasis</th>
                    <th>Año</th>
                    <th>Marca</th>
                    <th>Modelo</th>
                    <th>Color</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <%
                if (vehiculos.length() > 0) {
                    for (int i = 0; i < vehiculos.length(); i++) {
                        JSONObject v = vehiculos.getJSONObject(i);
                %>
                <tr>
                    <td><%= v.optInt("id") %></td>
                    <td><%= html(v.optString("placa")) %></td>
                    <td><%= html(v.optString("chasis")) %></td>
                    <td><%= v.optInt("anio") %></td>
                    <td><%= html(v.optString("marca")) %></td>
                    <td><%= html(v.optString("modelo")) %></td>
                    <td><%= html(v.optString("color")) %></td>
                    <td>
                        <button class="btn-open" data-vehiculo="<%= html(v.toString()) %>"
                                onclick="editarDesdeBoton(this)">Editar</button>
                        <button class="btn-open btn-danger" onclick="eliminar(<%= v.optInt("id") %>)">Eliminar</button>
                    </td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr><td colspan="8" style="text-align:center;">No hay vehículos registrados</td></tr>
                <% } %>
            </tbody>
        </table>

        <div class="modal-bg" id="modalInsert">
            <div class="modal">
                <button class="close-btn" onclick="cerrarModal('modalInsert')">X</button>
                <h2>Insertar Vehículo</h2>
                <form id="formInsert" onsubmit="insertarVehiculo(event)">
                    <input required type="text" name="placa" placeholder="Placa" maxlength="8" title="La placa debe tener máximo 8 caracteres">
                    <input required type="text" name="chasis" placeholder="Chasis" maxlength="17" title="El chasis admite hasta 17 caracteres">
                    <input required type="number" name="anio" placeholder="Año" min="1950" max="<%= java.time.Year.now().getValue() %>" title="El año del auto debe estar entre 1950 y el año actual">

                    <!-- Removido name="marca" para que no se envíe al backend -->
                    <select id="marcaInsert" required onchange="cargarModelos('marcaInsert', 'modeloInsert')">
                        <option value="">Seleccione Marca</option>
                    </select>

                    <select name="modelo" id="modeloInsert" required>
                        <option value="">Seleccione primero la marca</option>
                    </select>

                    <select name="color" id="colorInsert" required>
                        <option value="">Seleccione Color</option>
                    </select>

                    <input type="submit" value="Guardar">
                </form>
            </div>
        </div>

        <div class="modal-bg" id="modalEdit">
            <div class="modal">
                <button class="close-btn" onclick="cerrarModal('modalEdit')">X</button>
                <h2>Editar Vehículo</h2>
                <form id="formEdit" onsubmit="editarVehiculo(event)">
                    <input type="hidden" name="id" id="editId">

                    <label>Placa:</label>
                    <input required type="text" name="placa" id="editPlaca" maxlength="8" title="La placa debe tener máximo 8 caracteres">
                    <label>Chasis:</label>
                    <input required type="text" name="chasis" id="editChasis" placeholder="Chasis" maxlength="17" title="El chasis admite hasta 17 caracteres">
                    <label>Año:</label>
                    <input required type="number" name="anio" id="editAnio" min="1950" max="<%= java.time.Year.now().getValue() %>" title="El año del auto debe estar entre 1950 y el año actual" >

                    <label>Marca:</label>
                    <!-- Removido name="marca" para que no se envíe al backend -->
                    <select id="marcaEdit" required onchange="cargarModelos('marcaEdit', 'modeloEdit')"></select>

                    <label>Modelo:</label>
                    <select name="modelo" id="modeloEdit" required></select>

                    <label>Color:</label>
                    <select name="color" id="colorEdit" required></select>

                    <input type="submit" value="Actualizar Cambios">
                </form>
            </div>
        </div>

        <div class="modal-bg" id="modalEliminar">
            <div class="modal" style="text-align: center;">
                <h3>¿Está seguro de eliminar este vehículo?</h3>
                <button class="btn-open btn-danger" onclick="confirmarEliminarFinal()">Sí, Eliminar</button>
                <button class="btn-open" onclick="cerrarModal('modalEliminar')">Cancelar</button>
            </div>
        </div>

        <script>
            function editarDesdeBoton(boton) {
                const v = JSON.parse(boton.dataset.vehiculo);
                abrirEditar(v.id, v.placa, v.chasis, v.anio, v.id_marca, v.id_modelo, v.id_color,
                    v.marca, v.modelo, v.color);
            }

            // Utilidad: seleccionar opción por su texto visible cuando no se dispone de ID
            function seleccionarPorTexto(selectId, textoObjetivo) {
                const sel = document.getElementById(selectId);
                if (!sel || !textoObjetivo) return;
                const target = (textoObjetivo || "").trim().toLowerCase();
                for (let i = 0; i < sel.options.length; i++) {
                    const optText = (sel.options[i].textContent || sel.options[i].innerText || "").trim().toLowerCase();
                    if (optText === target) {
                        sel.selectedIndex = i;
                        return;
                    }
                }
            }
            // URLs relativas a tus Servlets
            const URL_SV_MARCA = "SvMarca";
            const URL_SV_MODELO = "SvModelo";
            const URL_SV_COLOR = "SvColor";
            const URL_SV_VEHICULO = "SvVehiculo";

            // --- CARGAR MARCAS ---
            // JSON esperado: [{"id":1,"marca":"Toyota"}]
            function cargarMarcas(selectId, idSeleccionado = null) {
                return fetch(URL_SV_MARCA)
                        .then(r => {
                            if (!r.ok) throw new Error("Error HTTP " + r.status);
                            return r.json();
                        })
                        .then(data => {
                            console.log("Marcas recibidas:", data);
                            let select = document.getElementById(selectId);
                            select.innerHTML = "";

                            // Agregar opción por defecto
                            let optDefault = document.createElement("option");
                            optDefault.value = "";
                            optDefault.textContent = "Seleccione una marca";
                            select.appendChild(optDefault);

                            if (Array.isArray(data)) {
                                data.forEach(m => {
                                    let opt = document.createElement("option");
                                    opt.value = m.id;
                                    opt.textContent = m.marca;
                                    if (idSeleccionado && m.id == idSeleccionado) {
                                        opt.selected = true;
                                    }
                                    select.appendChild(opt);
                                });
                            }
                        })
                        .catch(e => console.error("Error cargando marcas:", e));
            }

            // --- CARGAR COLORES ---
            // JSON esperado: [{"id":1,"color":"Azul"}]
            function cargarColores(selectId, idSeleccionado = null) {
                return fetch(URL_SV_COLOR)
                        .then(r => {
                            if (!r.ok) throw new Error("Error HTTP " + r.status);
                            return r.json();
                        })
                        .then(data => {
                            console.log("Colores recibidos:", data);
                            let select = document.getElementById(selectId);
                            select.innerHTML = "";

                            // Agregar opción por defecto
                            let optDefault = document.createElement("option");
                            optDefault.value = "";
                            optDefault.textContent = "Seleccione un color";
                            select.appendChild(optDefault);

                            if (Array.isArray(data)) {
                                data.forEach(c => {
                                    let opt = document.createElement("option");
                                    opt.value = c.id;
                                    opt.textContent = c.color;
                                    if (idSeleccionado && c.id == idSeleccionado) {
                                        opt.selected = true;
                                    }
                                    select.appendChild(opt);
                                });
                            }
                        })
                        .catch(e => console.error("Error cargando colores:", e));
            }

            // --- CARGAR MODELOS (Dependiente de Marca) ---
            // JSON esperado: [{"id":1,"modelo":"Corolla ","id_marca":1}]
            function cargarModelos(selectMarcaId, selectModeloId, idModeloSeleccionado = null) {
                let idMarca = document.getElementById(selectMarcaId).value;
                let selectModelo = document.getElementById(selectModeloId);

                selectModelo.innerHTML = '<option value="">Cargando modelos...</option>';
                selectModelo.disabled = true;
                if (!idMarca) {
                    selectModelo.disabled = false;
                    selectModelo.innerHTML = "";
                    let optDefault = document.createElement("option");
                    optDefault.value = "";
                    optDefault.textContent = "Seleccione una marca primero";
                    selectModelo.appendChild(optDefault);
                    return;
                }

                return fetch(URL_SV_MODELO + "?id=" + encodeURIComponent(idMarca))
                        .then(r => {
                            if (!r.ok) throw new Error("Error HTTP " + r.status);
                            return r.json();
                        })
                        .then(data => {
                            if (document.getElementById(selectMarcaId).value !== idMarca) return;
                            selectModelo.disabled = false;
                            console.log("Modelos recibidos:", data);
                            selectModelo.innerHTML = "";

                            // Agregar opción por defecto
                            let optDefault = document.createElement("option");
                            optDefault.value = "";
                            optDefault.textContent = "Seleccione un modelo";
                            selectModelo.appendChild(optDefault);

                            if (Array.isArray(data)) {
                                data.forEach(m => {
                                    let opt = document.createElement("option");
                                    opt.value = m.id;
                                    opt.textContent = m.modelo ? m.modelo.trim() : "Sin nombre";
                                    if (idModeloSeleccionado && m.id == idModeloSeleccionado) {
                                        opt.selected = true;
                                    }
                                    selectModelo.appendChild(opt);
                                });
                            }
                        })
                        .catch(e => {
                            if (document.getElementById(selectMarcaId).value !== idMarca) return;
                            selectModelo.innerHTML = '<option value="">No se pudieron cargar los modelos</option>';
                            selectModelo.disabled = false;
                            alert("No se pudieron cargar los modelos. Compruebe la conexión con la API.");
                        });
            }

            // --- ABRIR MODAL INSERTAR ---
            async function abrirInsertar() {
                // Limpiar formulario y selects
                document.getElementById("formInsert").reset();
                document.getElementById("modeloInsert").innerHTML = "";
                let optDefault = document.createElement("option");
                optDefault.value = "";
                optDefault.textContent = "Seleccione primero la marca";
                document.getElementById("modeloInsert").appendChild(optDefault);

                // Cargar listas antes de abrir el modal
                await cargarMarcas("marcaInsert");
                await cargarColores("colorInsert");

                // Ahora abrir el modal
                abrirModal("modalInsert");
            }

            // --- INSERTAR (POST) ---
            function insertarVehiculo(e) {
                e.preventDefault();
                let form = document.getElementById("formInsert");
                // Convertir FormData a URLSearchParams para enviar como application/x-www-form-urlencoded
                let datos = new URLSearchParams(new FormData(form));

                // Debug: Mostrar los datos que se van a enviar
                console.log("Datos a insertar:", datos.toString());

                fetch(URL_SV_VEHICULO, {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: datos
                }).then(res => {
                    console.log("Response status:", res.status, res.ok);
                    if (res.ok) {
                        cerrarModal("modalInsert");
                        alert("Vehículo insertado correctamente");
                        setTimeout(() => {
                            window.location.href = URL_SV_VEHICULO;
                        }, 100);
                    } else {
                        alert("Error al insertar el vehículo (Status: " + res.status + ")");
                    }
                }).catch(err => {
                    console.error("Error:", err);
                    alert("Error de conexión al insertar");
                });
            }

            // --- ABRIR MODAL EDITAR ---
            // Usamos async/await para asegurar que los combos se llenen antes de seleccionar el valor
            async function abrirEditar(id, placa, chasis, anio, marcaId, modeloId, colorId, marcaNombre, modeloNombre, colorNombre) {
                abrirModal("modalEdit");

                document.getElementById("editId").value = id;
                document.getElementById("editPlaca").value = placa;
                document.getElementById("editChasis").value = chasis;
                document.getElementById("editAnio").value = anio;

                // 1. Cargar Marcas y Colores y esperar a que terminen
                await cargarMarcas("marcaEdit", marcaId);
                await cargarColores("colorEdit", colorId);
                
                
                // 2. Asegurar visualmente que la marca está seleccionada (por si acaso el atributo 'selected' falló)
                const selMarca = document.getElementById("marcaEdit");
                if (marcaId) selMarca.value = marcaId;
                if (!selMarca.value && marcaNombre) seleccionarPorTexto("marcaEdit", marcaNombre);

                // 3. Cargar Modelos basándonos en la marca seleccionada y pre-seleccionar el modelo
                await cargarModelos("marcaEdit", "modeloEdit", modeloId);
                const selModelo = document.getElementById("modeloEdit");
                if (modeloId) selModelo.value = modeloId;
                if (!selModelo.value && modeloNombre) seleccionarPorTexto("modeloEdit", modeloNombre);

                const selColor = document.getElementById("colorEdit");
                if (colorId) selColor.value = colorId;
                if (!selColor.value && colorNombre) seleccionarPorTexto("colorEdit", colorNombre);
            }

            // --- EDITAR (PUT) ---
            function editarVehiculo(e) {
                e.preventDefault();
                const form = document.getElementById("formEdit");
                const datos = new URLSearchParams(new FormData(form));

                // Debug: Mostrar los datos que se van a enviar
                console.log("Datos a actualizar:", datos.toString());

                fetch(URL_SV_VEHICULO + "?" + datos.toString(), {
                    method: "PUT"
                }).then(res => {
                    console.log("Response status:", res.status, res.ok);
                    if (res.ok) {
                        cerrarModal("modalEdit");
                        alert("Vehículo actualizado correctamente");
                        setTimeout(() => {
                            window.location.href = URL_SV_VEHICULO;
                        }, 100);
                    } else {
                        alert("Error al actualizar el vehículo (Status: " + res.status + ")");
                    }
                }).catch(err => {
                    console.error("Error:", err);
                    alert("Error de conexión al actualizar");
                });
            }

            // --- ELIMINAR (DELETE) ---
            let idAEliminar = null;
            function eliminar(id) {
                idAEliminar = id;
                abrirModal("modalEliminar");
            }

            function confirmarEliminarFinal() {
                fetch(URL_SV_VEHICULO + "?id=" + idAEliminar, {
                    method: "DELETE"
                }).then(res => {
                    if (res.ok) {
                        cerrarModal("modalEliminar");
                        alert("Vehículo eliminado correctamente");
                        setTimeout(() => {
                            window.location.href = URL_SV_VEHICULO;
                        }, 100);
                    } else {
                        alert("Error al eliminar el vehículo");
                    }
                }).catch(err => {
                    console.error("Error:", err);
                    alert("Error de conexión al eliminar");
                });
            }

            // --- BUSCAR POR PLACA ---
            function buscarPorPlaca() {
                const placa = document.getElementById("placaBuscar").value.trim();
                if (!placa) {
                    alert("Por favor ingrese una placa");
                    return;
                }
                window.location.href = URL_SV_VEHICULO + "?placa=" + encodeURIComponent(placa);
            }

            // --- UTILIDADES MODAL ---
            function abrirModal(id) {
                document.getElementById(id).style.display = "flex";
            }
            function cerrarModal(id) {
                document.getElementById(id).style.display = "none";
            }
        </script>

    </body>
</html>