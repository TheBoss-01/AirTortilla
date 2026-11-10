document.addEventListener('DOMContentLoaded', () => {
  // Estado de la aplicacion
  let modoActual = 'total';
  let reservasParaPaso = [];
  let indicePasoActual = 0;
  let temporizadorAuto = null;
  let cacheCoincidencias = new Map();
  let mapaLocalizadores = new Map();
  let usuarioAutenticado = null;

  // Elementos DOM - Pestañas y Vistas
  const pestanaReservas = document.getElementById('pestanaReservas');
  const pestanaUsuarios = document.getElementById('pestanaUsuarios');
  const pestanaAuditoria = document.getElementById('pestanaAuditoria');
  const pestanaBdRelacional = document.getElementById('pestanaBdRelacional');

  const vistaReservas = document.getElementById('vistaReservas');
  const vistaUsuarios = document.getElementById('vistaUsuarios');
  const vistaAuditoria = document.getElementById('vistaAuditoria');
  const vistaBdRelacional = document.getElementById('vistaBdRelacional');

  // Elementos DOM - Sesion y Autenticacion (US1)
  const btnAbrirLogin = document.getElementById('btnAbrirLogin');
  const infoUsuarioConectado = document.getElementById('infoUsuarioConectado');
  const badgeRolUsuario = document.getElementById('badgeRolUsuario');
  const nombreUsuarioConectado = document.getElementById('nombreUsuarioConectado');
  const btnCerrarSesion = document.getElementById('btnCerrarSesion');

  const modalLogin = document.getElementById('modalLogin');
  const btnCerrarModalLogin = document.getElementById('btnCerrarModalLogin');
  const formularioLogin = document.getElementById('formularioLogin');
  const inputLoginUsuario = document.getElementById('inputLoginUsuario');
  const inputLoginPassword = document.getElementById('inputLoginPassword');
  const mensajeErrorLogin = document.getElementById('mensajeErrorLogin');
  const btnAutoLoginAdmin = document.getElementById('btnAutoLoginAdmin');
  const btnAutoLoginEmpleado = document.getElementById('btnAutoLoginEmpleado');

  // Elementos DOM - Gestion de Usuarios (US2)
  const avisoNoAdminUsuarios = document.getElementById('avisoNoAdminUsuarios');
  const contenidoAdminUsuarios = document.getElementById('contenidoAdminUsuarios');
  const formAltaUsuario = document.getElementById('formAltaUsuario');
  const nuevoUsername = document.getElementById('nuevoUsername');
  const nuevoPassword = document.getElementById('nuevoPassword');
  const nuevoRol = document.getElementById('nuevoRol');
  const nuevoNombreCompleto = document.getElementById('nuevoNombreCompleto');
  const btnGuardarUsuario = document.getElementById('btnGuardarUsuario');
  const cuerpoTablaUsuarios = document.getElementById('cuerpoTablaUsuarios');

  // Elementos DOM - Trazabilidad y Auditoria (US3)
  const avisoNoAdminAuditoria = document.getElementById('avisoNoAdminAuditoria');
  const contenidoAdminAuditoria = document.getElementById('contenidoAdminAuditoria');
  const cuerpoTablaAuditoria = document.getElementById('cuerpoTablaAuditoria');
  const btnRefrescarAuditoria = document.getElementById('btnRefrescarAuditoria');

  // Elementos DOM - Migracion Relacional
  const btnEjecutarMigracionBd = document.getElementById('btnEjecutarMigracionBd');
  const btnVerReservasCsv = document.getElementById('btnVerReservasCsv');
  const btnVerReservasBd = document.getElementById('btnVerReservasBd');

  // Elementos DOM - Procesamiento y Controles (Entrega 1)
  const btnModoTotal = document.getElementById('btnModoTotal');
  const btnModoLinea = document.getElementById('btnModoLinea');
  const btnCrearFicheroPais = document.getElementById('btnCrearFicheroPais');
  const btnGenerarPruebas = document.getElementById('btnGenerarPruebas');
  const controlesPasoAPaso = document.getElementById('controlesPasoAPaso');
  const btnPasoSiguiente = document.getElementById('btnPasoSiguiente');
  const btnPasoAnterior = document.getElementById('btnPasoAnterior');
  const btnAutoPlay = document.getElementById('btnAutoPlay');
  const btnReiniciarPaso = document.getElementById('btnReiniciarPaso');
  const textoProgresoLinea = document.getElementById('textoProgresoLinea');
  const barraProgresoRelleno = document.getElementById('barraProgresoRelleno');

  const metricaTotal = document.getElementById('metricaTotal');
  const metricaCoincidencias = document.getElementById('metricaCoincidencias');
  const metricaFicheros = document.getElementById('metricaFicheros');
  const badgeEstadoProceso = document.getElementById('badgeEstadoProceso');
  const cuerpoConsola = document.getElementById('cuerpoConsola');
  const btnLimpiarConsola = document.getElementById('btnLimpiarConsola');
  const listaGruposCoincidentes = document.getElementById('listaGruposCoincidentes');
  const contenedorDetalleGrupos = document.getElementById('contenedorDetalleGrupos');
  const rejillaArchivos = document.getElementById('rejillaArchivos');
  const btnRefrescarArchivos = document.getElementById('btnRefrescarArchivos');
  const cuerpoTablaReservas = document.getElementById('cuerpoTablaReservas');

  const modalPrevisualizacion = document.getElementById('modalPrevisualizacion');
  const modalTituloArchivo = document.getElementById('modalTituloArchivo');
  const modalCuerpoCsv = document.getElementById('modalCuerpoCsv');
  const btnCerrarModal = document.getElementById('btnCerrarModal');
  const btnDescargarCsvModal = document.getElementById('btnDescargarCsvModal');

  // =========================================================================
  // GESTION DE PESTAÑAS
  // =========================================================================
  const pestanas = [
    { boton: pestanaReservas, vista: vistaReservas },
    { boton: pestanaUsuarios, vista: vistaUsuarios, alActivar: cargarListaUsuarios },
    { boton: pestanaAuditoria, vista: vistaAuditoria, alActivar: cargarListaAuditoria },
    { boton: pestanaBdRelacional, vista: vistaBdRelacional }
  ];

  pestanas.forEach(({ boton, vista, alActivar }) => {
    boton.addEventListener('click', () => {
      pestanas.forEach(p => {
        p.boton.classList.remove('activo');
        p.vista.classList.add('oculta');
      });
      boton.classList.add('activo');
      vista.classList.remove('oculta');
      if (alActivar) alActivar();
    });
  });

  // =========================================================================
  // CONTROL DE SESION Y LOGIN (US1)
  // =========================================================================
  btnAbrirLogin.addEventListener('click', () => {
    mensajeErrorLogin.style.display = 'none';
    modalLogin.classList.remove('oculta');
  });

  btnCerrarModalLogin.addEventListener('click', () => modalLogin.classList.add('oculta'));
  modalLogin.addEventListener('click', (e) => {
    if (e.target === modalLogin) modalLogin.classList.add('oculta');
  });

  btnAutoLoginAdmin.addEventListener('click', () => {
    inputLoginUsuario.value = 'admin';
    inputLoginPassword.value = 'admin123';
  });

  btnAutoLoginEmpleado.addEventListener('click', () => {
    inputLoginUsuario.value = 'empleado';
    inputLoginPassword.value = 'empleado123';
  });

  formularioLogin.addEventListener('submit', async (e) => {
    e.preventDefault();
    await ejecutarLogin(inputLoginUsuario.value, inputLoginPassword.value);
  });

  btnCerrarSesion.addEventListener('click', async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST' });
    } catch (e) {
      console.warn(e);
    }
    usuarioAutenticado = null;
    actualizarInterfazUsuario();
    logConsola('[SEGURIDAD] Sesión finalizada correctamente.', 'info');
  });

  async function comprobarEstadoSesion() {
    try {
      const resp = await fetch('/api/auth/estado');
      if (resp.ok) {
        const data = await resp.json();
        if (data.autenticado) {
          usuarioAutenticado = data.usuario;
        } else {
          usuarioAutenticado = null;
        }
      }
    } catch (e) {
      console.warn('Servidor no disponible para consultar sesión');
    }
    actualizarInterfazUsuario();
  }

  async function ejecutarLogin(username, password) {
    mensajeErrorLogin.style.display = 'none';
    try {
      const resp = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await resp.json();
      if (resp.ok && data.exito) {
        usuarioAutenticado = data.usuario;
        modalLogin.classList.add('oculta');
        actualizarInterfazUsuario();
        logConsola(`[LOGIN] Bienvenido/a, ${data.usuario.nombreCompleto} (Rol: ${data.usuario.rol}).`, 'exito');
      } else {
        mensajeErrorLogin.textContent = data.mensaje || 'Credenciales incorrectas';
        mensajeErrorLogin.style.display = 'block';
      }
    } catch (err) {
      mensajeErrorLogin.textContent = 'Error de conexión con el servidor';
      mensajeErrorLogin.style.display = 'block';
    }
  }

  function actualizarInterfazUsuario() {
    if (usuarioAutenticado) {
      btnAbrirLogin.classList.add('oculta');
      infoUsuarioConectado.classList.remove('oculta');
      nombreUsuarioConectado.textContent = usuarioAutenticado.nombreCompleto || usuarioAutenticado.username;
      badgeRolUsuario.textContent = usuarioAutenticado.rol;
      if (usuarioAutenticado.rol === 'ADMIN') {
        badgeRolUsuario.className = 'badge-rol badge-rol-admin';
        avisoNoAdminUsuarios.classList.add('oculta');
        contenidoAdminUsuarios.classList.remove('oculta');
        avisoNoAdminAuditoria.classList.add('oculta');
        contenidoAdminAuditoria.classList.remove('oculta');
      } else {
        badgeRolUsuario.className = 'badge-rol badge-rol-empleado';
        avisoNoAdminUsuarios.classList.remove('oculta');
        contenidoAdminUsuarios.classList.add('oculta');
        avisoNoAdminAuditoria.classList.remove('oculta');
        contenidoAdminAuditoria.classList.add('oculta');
      }
    } else {
      btnAbrirLogin.classList.remove('oculta');
      infoUsuarioConectado.classList.add('oculta');
      avisoNoAdminUsuarios.classList.remove('oculta');
      contenidoAdminUsuarios.classList.add('oculta');
      avisoNoAdminAuditoria.classList.remove('oculta');
      contenidoAdminAuditoria.classList.add('oculta');
    }
  }

  // =========================================================================
  // GESTION DE USUARIOS (US2 - ADMINISTRADOR)
  // =========================================================================
  btnGuardarUsuario.addEventListener('click', async () => {
    const username = nuevoUsername.value.trim();
    const password = nuevoPassword.value.trim();
    const rol = nuevoRol.value;
    const nombreCompleto = nuevoNombreCompleto.value.trim();

    if (!username || !password || !nombreCompleto) {
      alert('Por favor complete todos los campos obligatorios.');
      return;
    }

    try {
      const resp = await fetch('/api/usuarios/alta', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password, rol, nombreCompleto })
      });
      const data = await resp.json();
      if (resp.ok && data.exito) {
        logConsola(`[ALTA USUARIO] Usuario '${username}' registrado con rol ${rol}.`, 'exito');
        nuevoUsername.value = '';
        nuevoPassword.value = '';
        nuevoNombreCompleto.value = '';
        await cargarListaUsuarios();
        alert('Usuario dado de alta correctamente en la base de datos relacional.');
      } else {
        alert('Error: ' + (data.mensaje || 'No se pudo crear el usuario'));
      }
    } catch (e) {
      alert('Error de conexión al registrar usuario');
    }
  });

  async function cargarListaUsuarios() {
    if (!usuarioAutenticado || usuarioAutenticado.rol !== 'ADMIN') {
      return;
    }
    cuerpoTablaUsuarios.innerHTML = '<tr><td colspan="6" class="celda-vacia">Consultando tabla relacional \'usuarios\'...</td></tr>';
    try {
      const resp = await fetch('/api/usuarios');
      if (!resp.ok) throw new Error('No autorizado');
      const usuarios = await resp.json();
      if (usuarios.length === 0) {
        cuerpoTablaUsuarios.innerHTML = '<tr><td colspan="6" class="celda-vacia">No hay usuarios registrados.</td></tr>';
        return;
      }
      cuerpoTablaUsuarios.innerHTML = usuarios.map(u => `
        <tr>
          <td>#${u.id}</td>
          <td><strong>${escaparHtml(u.username)}</strong></td>
          <td><span class="badge-rol ${u.rol === 'ADMIN' ? 'badge-rol-admin' : 'badge-rol-empleado'}">${u.rol}</span></td>
          <td>${escaparHtml(u.nombreCompleto)}</td>
          <td>${escaparHtml(u.fechaCreacion || '-')}</td>
          <td><span style="color: ${u.activo ? 'var(--color-exito)' : 'var(--color-peligro)'}">● ${u.activo ? 'Activo' : 'Inactivo'}</span></td>
        </tr>
      `).join('');
    } catch (err) {
      cuerpoTablaUsuarios.innerHTML = '<tr><td colspan="6" class="celda-vacia">Inicie sesión como Administrador para ver usuarios.</td></tr>';
    }
  }

  // =========================================================================
  // TRAZABILIDAD Y AUDITORIA (US3 - ADMINISTRADOR)
  // =========================================================================
  btnRefrescarAuditoria.addEventListener('click', cargarListaAuditoria);

  async function cargarListaAuditoria() {
    if (!usuarioAutenticado || usuarioAutenticado.rol !== 'ADMIN') {
      return;
    }
    cuerpoTablaAuditoria.innerHTML = '<tr><td colspan="7" class="celda-vacia">Consultando bitácora de auditoría...</td></tr>';
    try {
      const resp = await fetch('/api/auditoria');
      if (!resp.ok) throw new Error('No autorizado');
      const logs = await resp.json();
      if (logs.length === 0) {
        cuerpoTablaAuditoria.innerHTML = '<tr><td colspan="7" class="celda-vacia">No hay registros de trazabilidad todavía.</td></tr>';
        return;
      }
      cuerpoTablaAuditoria.innerHTML = logs.map(l => {
        let claseBadge = 'badge-accion-otro';
        if (l.accion.includes('LOGIN')) claseBadge = 'badge-accion-login';
        else if (l.accion.includes('USUARIO')) claseBadge = 'badge-accion-usuario';
        else if (l.accion.includes('MIGRACION') || l.accion.includes('RESERVAS')) claseBadge = 'badge-accion-migracion';

        return `
          <tr>
            <td>#${l.id}</td>
            <td><small>${escaparHtml(l.fechaHora || '-')}</small></td>
            <td><strong>${escaparHtml(l.usuario)}</strong></td>
            <td><span class="badge-accion ${claseBadge}">${escaparHtml(l.accion)}</span></td>
            <td><small>${escaparHtml(l.entidad)}</small></td>
            <td>${escaparHtml(l.detalles || '')}</td>
            <td><small>${escaparHtml(l.ipOrigen || '-')}</small></td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      cuerpoTablaAuditoria.innerHTML = '<tr><td colspan="7" class="celda-vacia">Inicie sesión como Administrador para consultar la trazabilidad.</td></tr>';
    }
  }

  // =========================================================================
  // MIGRACION Y PERSISTENCIA RELACIONAL
  // =========================================================================
  btnEjecutarMigracionBd.addEventListener('click', async () => {
    actualizarBadgeEstado('procesando', 'Migrando a BD...');
    logConsola('[LEGISLACIÓN] Ejecutando migración de reservas a Base de Datos Relacional...', 'aviso');
    try {
      const resp = await fetch('/api/bd/migrar', { method: 'POST' });
      const data = await resp.json();
      if (resp.ok && data.exito) {
        actualizarBadgeEstado('exito', 'Migración Completa');
        logConsola(`[BD RELACIONAL] Se han persistido ${data.totalMigradas} reservas en SQLite.`, 'exito');
        alert(`¡Migración completada con éxito! Se han sincronizado ${data.totalMigradas} reservas en la base de datos relacional.`);
        await cargarReservasRelacionales();
      } else {
        throw new Error(data.mensaje || 'Fallo en la migración');
      }
    } catch (e) {
      actualizarBadgeEstado('error', 'Error Migración');
      logConsola('[ERROR BD] ' + e.message, 'error');
      alert('Error durante la migración: ' + e.message);
    }
  });

  btnVerReservasCsv.addEventListener('click', async () => {
    logConsola('Consultando reservas desde origen CSV original...', 'info');
    await cargarVistaPreviaInicial();
  });

  btnVerReservasBd.addEventListener('click', async () => {
    logConsola('Consultando reservas persistidas en Base de Datos Relacional (SQLite)...', 'info');
    await cargarReservasRelacionales();
  });

  async function cargarReservasRelacionales() {
    try {
      const resp = await fetch('/api/bd/reservas');
      if (!resp.ok) throw new Error('Error al consultar reservas en BD');
      const reservas = await resp.json();
      renderizarTablaReservas(reservas);
      logConsola(`[BD RELACIONAL] Mostrando ${reservas.length} reservas desde SQLite.`, 'info');
    } catch (e) {
      logConsola('[ERROR] No se pudieron cargar reservas desde la BD: ' + e.message, 'error');
    }
  }

  // =========================================================================
  // FUNCIONALIDAD ENTREGA 1 (MODO TOTAL Y LINEA A LINEA)
  // =========================================================================
  btnModoTotal.addEventListener('click', () => cambiarModo('total'));
  btnModoLinea.addEventListener('click', () => cambiarModo('linea'));
  btnCrearFicheroPais.addEventListener('click', ejecutarAccionPrincipal);
  btnGenerarPruebas.addEventListener('click', generarDatosPrueba);
  btnRefrescarArchivos.addEventListener('click', cargarListaArchivos);
  btnLimpiarConsola.addEventListener('click', () => cuerpoConsola.innerHTML = '');

  btnPasoSiguiente.addEventListener('click', avanzarPasoSiguiente);
  btnPasoAnterior.addEventListener('click', retrocederPaso);
  btnAutoPlay.addEventListener('click', toggleAutoPlay);
  btnReiniciarPaso.addEventListener('click', reiniciarPasoAPaso);

  btnCerrarModal.addEventListener('click', () => modalPrevisualizacion.classList.add('oculta'));
  modalPrevisualizacion.addEventListener('click', (e) => {
    if (e.target === modalPrevisualizacion) {
      modalPrevisualizacion.classList.add('oculta');
    }
  });

  function cambiarModo(nuevoModo) {
    modoActual = nuevoModo;
    if (nuevoModo === 'total') {
      btnModoTotal.classList.add('activo');
      btnModoLinea.classList.remove('activo');
      controlesPasoAPaso.classList.add('oculta');
      btnCrearFicheroPais.innerHTML = '<span class="icono-accion">📁</span><span>Crear Fichero x País Destino (Total)</span>';
      detenerAutoPlay();
      logConsola('Modo cambiado a TOTAL. Todo se procesará en un único ciclo.', 'info');
    } else {
      btnModoLinea.classList.add('activo');
      btnModoTotal.classList.remove('activo');
      controlesPasoAPaso.classList.remove('oculta');
      btnCrearFicheroPais.innerHTML = '<span class="icono-accion">🚀</span><span>Iniciar Modo Línea a Línea</span>';
      logConsola('Modo cambiado a LÍNEA A LÍNEA. Podrá avanzar registro a registro.', 'info');
    }
  }

  async function ejecutarAccionPrincipal() {
    if (modoActual === 'total') {
      await ejecutarModoTotal();
    } else {
      await prepararModoLineaALinea();
    }
  }

  async function ejecutarModoTotal() {
    actualizarBadgeEstado('procesando', 'Procesando...');
    logConsola('Iniciando procesamiento en MODALIDAD TOTAL...', 'aviso');

    try {
      const respuesta = await fetch('/api/procesar-total', { method: 'POST' });
      if (!respuesta.ok) throw new Error('Error en el servidor al procesar reservas');
      const datos = await respuesta.json();

      actualizarBadgeEstado('exito', 'Completado');
      metricaTotal.textContent = datos.totalReservas;
      metricaCoincidencias.textContent = datos.totalCoincidencias;
      metricaFicheros.textContent = datos.ficheros ? datos.ficheros.length : 0;

      logConsola(`[ÉXITO] ${datos.totalReservas} reservas procesadas con MergeSort O(n log n).`, 'exito');
      logConsola(`[COINCIDENCIAS] Detectados ${datos.totalCoincidencias} grupos con localizador unívoco.`, 'info');
      logConsola(`[BD RELACIONAL] Reservas sincronizadas con la tabla SQLite.`, 'exito');

      renderizarDetalleCoincidencias(datos.localizadores);
      renderizarTablaReservas(datos.reservas);
      await cargarListaArchivos();
    } catch (error) {
      actualizarBadgeEstado('error', 'Error');
      logConsola('[ERROR] ' + error.message, 'error');
    }
  }

  async function prepararModoLineaALinea() {
    actualizarBadgeEstado('procesando', 'Cargando...');
    logConsola('Cargando datos para ejecución LÍNEA A LÍNEA...', 'aviso');

    try {
      const respuesta = await fetch('/api/leer-reservas');
      if (!respuesta.ok) throw new Error('Error al cargar reservas');
      reservasParaPaso = await respuesta.json();

      cacheCoincidencias.clear();
      mapaLocalizadores.clear();
      reservasParaPaso.forEach((r, idx) => {
        r.id = 'RES-' + String(idx + 1).padStart(4, '0');
        const clave = `${r.nombrePasajero}|${r.origen}|${r.destino}`.toLowerCase();
        if (!cacheCoincidencias.has(clave)) {
          cacheCoincidencias.set(clave, []);
        }
        cacheCoincidencias.get(clave).push(r);
      });

      let numLoc = 1000;
      cacheCoincidencias.forEach((lista, clave) => {
        if (lista.length > 1) {
          const loc = 'LOC-' + (++numLoc);
          mapaLocalizadores.set(clave, loc);
          lista.forEach(res => res.localizador = loc);
        } else {
          lista[0].localizador = 'N/A';
        }
      });

      reiniciarPasoAPaso();
      actualizarBadgeEstado('espera', 'Listo para avanzar');
      logConsola(`Preparado: ${reservasParaPaso.length} registros listos. Pulse [Siguiente Registro] o [Auto].`, 'info');
      btnPasoSiguiente.disabled = false;
      btnAutoPlay.disabled = false;
    } catch (error) {
      actualizarBadgeEstado('error', 'Error');
      logConsola('[ERROR] ' + error.message, 'error');
    }
  }

  function avanzarPasoSiguiente() {
    if (indicePasoActual >= reservasParaPaso.length) {
      detenerAutoPlay();
      actualizarBadgeEstado('exito', 'Finalizado');
      logConsola('Procesamiento línea a línea finalizado completamente.', 'exito');
      return;
    }

    const r = reservasParaPaso[indicePasoActual];
    indicePasoActual++;

    actualizarProgresoPaso();
    renderizarFilaIndividual(r, indicePasoActual === 1);

    logConsola(`[Reg ${indicePasoActual}] ${r.nombrePasajero} | Ruta: ${r.origen} ➔ ${r.destino} | ID: ${r.id} | Loc: ${r.localizador}`, 'info');

    if (indicePasoActual >= reservasParaPaso.length) {
      detenerAutoPlay();
      actualizarBadgeEstado('exito', 'Finalizado');
      btnPasoSiguiente.disabled = true;
    }
    btnPasoAnterior.disabled = indicePasoActual <= 1;
  }

  function retrocederPaso() {
    if (indicePasoActual > 0) {
      indicePasoActual--;
      actualizarProgresoPaso();
      const sublista = reservasParaPaso.slice(0, indicePasoActual);
      renderizarTablaReservas(sublista);
      btnPasoSiguiente.disabled = false;
      btnPasoAnterior.disabled = indicePasoActual <= 1;
    }
  }

  function toggleAutoPlay() {
    if (temporizadorAuto) {
      detenerAutoPlay();
    } else {
      btnAutoPlay.textContent = '⏸ Pausa';
      btnAutoPlay.classList.add('activo');
      temporizadorAuto = setInterval(() => {
        if (indicePasoActual >= reservasParaPaso.length) {
          detenerAutoPlay();
        } else {
          avanzarPasoSiguiente();
        }
      }, 500);
    }
  }

  function detenerAutoPlay() {
    if (temporizadorAuto) {
      clearInterval(temporizadorAuto);
      temporizadorAuto = null;
    }
    btnAutoPlay.textContent = '▶ Auto';
    btnAutoPlay.classList.remove('activo');
  }

  function reiniciarPasoAPaso() {
    detenerAutoPlay();
    indicePasoActual = 0;
    actualizarProgresoPaso();
    cuerpoTablaReservas.innerHTML = '<tr><td colspan="6" class="celda-vacia">Iniciando modo paso a paso...</td></tr>';
    btnPasoSiguiente.disabled = reservasParaPaso.length === 0;
    btnPasoAnterior.disabled = true;
  }

  function actualizarProgresoPaso() {
    const total = reservasParaPaso.length;
    textoProgresoLinea.textContent = `Registro ${indicePasoActual} de ${total}`;
    const porcentaje = total > 0 ? (indicePasoActual / total) * 100 : 0;
    barraProgresoRelleno.style.width = porcentaje + '%';
    metricaTotal.textContent = indicePasoActual;
  }

  function renderizarFilaIndividual(r, limpiar) {
    if (limpiar) {
      cuerpoTablaReservas.innerHTML = '';
    }
    const tr = document.createElement('tr');
    tr.className = 'fila-animada';
    tr.innerHTML = `
      <td><span class="etiqueta-id">${r.id}</span></td>
      <td><span class="${r.localizador && r.localizador !== 'N/A' ? 'etiqueta-loc' : 'etiqueta-vacia'}">${r.localizador}</span></td>
      <td><strong>${escaparHtml(r.nombrePasajero)}</strong></td>
      <td>${escaparHtml(r.origen)} ➔ ${escaparHtml(r.destino)}</td>
      <td><span class="badge-pais">${escaparHtml(r.codigoPaisDestino)}</span></td>
      <td>${r.fechaReserva}</td>
    `;
    cuerpoTablaReservas.appendChild(tr);
    tr.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function renderizarTablaReservas(lista) {
    if (!lista || lista.length === 0) {
      cuerpoTablaReservas.innerHTML = '<tr><td colspan="6" class="celda-vacia">Sin reservas para mostrar.</td></tr>';
      return;
    }
    cuerpoTablaReservas.innerHTML = lista.map(r => `
      <tr>
        <td><span class="etiqueta-id">${r.id || '-'}</span></td>
        <td><span class="${r.localizador && r.localizador !== 'N/A' ? 'etiqueta-loc' : 'etiqueta-vacia'}">${r.localizador || 'N/A'}</span></td>
        <td><strong>${escaparHtml(r.nombrePasajero)}</strong></td>
        <td>${escaparHtml(r.origen)} ➔ ${escaparHtml(r.destino)}</td>
        <td><span class="badge-pais">${escaparHtml(r.codigoPaisDestino)}</span></td>
        <td>${r.fechaReserva}</td>
      </tr>
    `).join('');
  }

  function renderizarDetalleCoincidencias(grupos) {
    if (!grupos || grupos.length === 0) {
      contenedorDetalleGrupos.classList.add('oculta');
      return;
    }
    contenedorDetalleGrupos.classList.remove('oculta');
    listaGruposCoincidentes.innerHTML = grupos.map(g => `
      <div class="tarjeta-grupo">
        <span class="etiqueta-loc">${g.localizador}</span>
        <strong>${g.pasajeros} Pasajeros</strong>
        <small>Mismo Pasajero, Origen y Destino</small>
      </div>
    `).join('');
  }

  async function cargarListaArchivos() {
    try {
      const resp = await fetch('/api/archivos');
      if (!resp.ok) throw new Error('Error al listar archivos');
      const archivos = await resp.json();

      if (archivos.length === 0) {
        rejillaArchivos.innerHTML = '<div class="mensaje-vacio">No hay ficheros generados todavía. Ejecute el procesamiento para crearlos.</div>';
        metricaFicheros.textContent = '0';
        return;
      }

      metricaFicheros.textContent = archivos.length;
      rejillaArchivos.innerHTML = archivos.map(a => `
        <div class="tarjeta-archivo">
          <div class="icono-archivo">📄</div>
          <div class="info-archivo">
            <strong>${a.nombre}</strong>
            <small>${a.registros} registros · ${formatearBytes(a.tamano)}</small>
          </div>
          <div class="acciones-archivo">
            <button class="btn-archivo-accion btn-ver" data-archivo="${a.nombre}">Ver</button>
            <a href="/api/descargar?archivo=${encodeURIComponent(a.nombre)}" class="btn-archivo-accion btn-descargar" download>Descargar</a>
          </div>
        </div>
      `).join('');

      document.querySelectorAll('.btn-ver').forEach(b => {
        b.addEventListener('click', () => abrirModalCsv(b.getAttribute('data-archivo')));
      });
    } catch (e) {
      console.error(e);
    }
  }

  async function abrirModalCsv(nombre) {
    modalTituloArchivo.textContent = nombre;
    modalCuerpoCsv.textContent = 'Cargando contenido...';
    btnDescargarCsvModal.href = `/api/descargar?archivo=${encodeURIComponent(nombre)}`;
    modalPrevisualizacion.classList.remove('oculta');

    try {
      const resp = await fetch(`/api/descargar?archivo=${encodeURIComponent(nombre)}`);
      const texto = await resp.text();
      modalCuerpoCsv.textContent = texto;
    } catch (e) {
      modalCuerpoCsv.textContent = 'Error al leer el archivo.';
    }
  }

  async function generarDatosPrueba() {
    try {
      logConsola('Generando fichero CSV con los casos de prueba...', 'aviso');
      const resp = await fetch('/api/generar-pruebas', { method: 'POST' });
      const data = await resp.json();
      logConsola('[PRUEBAS] ' + data.mensaje, 'exito');
      await cargarVistaPreviaInicial();
    } catch (e) {
      logConsola('[ERROR] No se pudo generar el fichero de prueba', 'error');
    }
  }

  async function cargarVistaPreviaInicial() {
    try {
      const resp = await fetch('/api/leer-reservas');
      if (resp.ok) {
        const reservas = await resp.json();
        metricaTotal.textContent = reservas.length;
        renderizarTablaReservas(reservas);
        logConsola(`CSV cargado: ${reservas.length} registros listos en memoria.`, 'info');
      }
    } catch (e) {
      console.warn('Servidor aún no disponible.');
    }
  }

  function actualizarBadgeEstado(tipo, texto) {
    badgeEstadoProceso.className = 'badge-estado';
    if (tipo === 'procesando') badgeEstadoProceso.classList.add('badge-procesando');
    else if (tipo === 'exito') badgeEstadoProceso.classList.add('badge-exito');
    else if (tipo === 'error') badgeEstadoProceso.classList.add('badge-error');
    else badgeEstadoProceso.classList.add('badge-espera');
    badgeEstadoProceso.textContent = texto;
  }

  function logConsola(mensaje, tipo = 'info') {
    const hora = new Date().toLocaleTimeString();
    const div = document.createElement('div');
    div.className = `linea-consola ${tipo}`;
    div.textContent = `[${hora}] ${mensaje}`;
    cuerpoConsola.appendChild(div);
    cuerpoConsola.scrollTop = cuerpoConsola.scrollHeight;
  }

  function formatearBytes(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const sizes = ['B', 'KB', 'MB'];
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  function escaparHtml(texto) {
    if (!texto) return '';
    return texto.replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Inicializacion automatica al cargar la pagina
  comprobarEstadoSesion();
  cargarVistaPreviaInicial();
  cargarListaArchivos();
});
