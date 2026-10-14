document.addEventListener('DOMContentLoaded', () => {
  let modoActual = 'total';
  let reservasParaPaso = [];
  let indicePasoActual = 0;
  let temporizadorAuto = null;
  let cacheCoincidencias = new Map();
  let mapaLocalizadores = new Map();

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

      renderizarGruposCoincidentes(datos.localizadores);
      renderizarTablaReservas(datos.reservas);
      await cargarListaArchivos();

    } catch (error) {
      actualizarBadgeEstado('espera', 'Error');
      logConsola(`[ERROR] ${error.message}`, 'error');
    }
  }

  async function prepararModoLineaALinea() {
    actualizarBadgeEstado('procesando', 'Cargando datos...');
    logConsola('Cargando dataset para ejecución paso a paso...', 'info');

    try {
      const respuesta = await fetch('/api/leer-reservas');
      if (!respuesta.ok) throw new Error('Error al cargar reservas');
      reservasParaPaso = await respuesta.json();

      indicePasoActual = 0;
      cacheCoincidencias.clear();
      mapaLocalizadores.clear();
      cuerpoTablaReservas.innerHTML = '';
      actualizarProgresoPaso();

      for (const r of reservasParaPaso) {
        const clave = (r.nombrePasajero + '|' + r.origen + '|' + r.destino).toLowerCase();
        cacheCoincidencias.set(clave, (cacheCoincidencias.get(clave) || 0) + 1);
      }

      actualizarBadgeEstado('procesando', 'Listo para avanzar');
      logConsola(`Se han cargado ${reservasParaPaso.length} registros. Pulse "Siguiente Registro" para procesar uno a uno.`, 'exito');
    } catch (error) {
      actualizarBadgeEstado('espera', 'Error');
      logConsola(`[ERROR] ${error.message}`, 'error');
    }
  }

  function avanzarPasoSiguiente() {
    if (indicePasoActual >= reservasParaPaso.length) {
      detenerAutoPlay();
      actualizarBadgeEstado('exito', 'Finalizado');
      logConsola('Todos los registros han sido procesados línea a línea.', 'exito');
      cargarListaArchivos();
      return;
    }

    const reserva = reservasParaPaso[indicePasoActual];
    const clave = (reserva.nombrePasajero + '|' + reserva.origen + '|' + reserva.destino).toLowerCase();
    const repeticiones = cacheCoincidencias.get(clave) || 1;

    let idGenerado = 'RES-' + Math.random().toString(36).substring(2, 10).toUpperCase();
    let localizador = 'N/A';

    if (repeticiones >= 2) {
      if (!mapaLocalizadores.has(clave)) {
        mapaLocalizadores.set(clave, 'LOC-' + Math.random().toString(36).substring(2, 8).toUpperCase());
      }
      localizador = mapaLocalizadores.get(clave);
    }

    reserva.id = idGenerado;
    reserva.localizador = localizador;

    agregarFilaTabla(reserva);

    logConsola(`[Registro ${indicePasoActual + 1}/${reservasParaPaso.length}] Pasajero: ${reserva.nombrePasajero} | Ruta: ${reserva.origen}➔${reserva.destino} | ID: ${idGenerado} | Loc: ${localizador}`, 'info');

    indicePasoActual++;
    actualizarProgresoPaso();

    metricaTotal.textContent = indicePasoActual;
    metricaCoincidencias.textContent = mapaLocalizadores.size;
  }

  function retrocederPaso() {
    if (indicePasoActual > 0) {
      indicePasoActual--;
      actualizarProgresoPaso();
      const filas = cuerpoTablaReservas.querySelectorAll('tr');
      if (filas.length > 0) {
        filas[filas.length - 1].remove();
      }
    }
  }

  function toggleAutoPlay() {
    if (temporizadorAuto) {
      detenerAutoPlay();
    } else {
      btnAutoPlay.textContent = '⏸ Pausa';
      btnAutoPlay.classList.add('btn-destacado');
      temporizadorAuto = setInterval(() => {
        if (indicePasoActual < reservasParaPaso.length) {
          avanzarPasoSiguiente();
        } else {
          detenerAutoPlay();
        }
      }, 400);
    }
  }

  function detenerAutoPlay() {
    if (temporizadorAuto) {
      clearInterval(temporizadorAuto);
      temporizadorAuto = null;
      btnAutoPlay.textContent = '▶ Auto';
      btnAutoPlay.classList.remove('btn-destacado');
    }
  }

  function reiniciarPasoAPaso() {
    detenerAutoPlay();
    indicePasoActual = 0;
    cuerpoTablaReservas.innerHTML = '';
    actualizarProgresoPaso();
    logConsola('Paso a paso reiniciado.', 'aviso');
  }

  function actualizarProgresoPaso() {
    const total = reservasParaPaso.length;
    const porcentaje = total > 0 ? (indicePasoActual / total) * 100 : 0;
    textoProgresoLinea.textContent = `Registro ${indicePasoActual} de ${total}`;
    barraProgresoRelleno.style.width = `${porcentaje}%`;
    btnPasoAnterior.disabled = indicePasoActual === 0;
    btnPasoSiguiente.disabled = total > 0 && indicePasoActual >= total;
  }

  async function cargarListaArchivos() {
    try {
      const respuesta = await fetch('/api/archivos');
      if (!respuesta.ok) return;
      const archivos = await respuesta.json();

      if (archivos.length === 0) {
        rejillaArchivos.innerHTML = '<div class="mensaje-vacio">No hay ficheros generados todavía. Ejecute el procesamiento para crearlos.</div>';
        metricaFicheros.textContent = '0';
        return;
      }

      metricaFicheros.textContent = archivos.length;
      rejillaArchivos.innerHTML = '';

      archivos.forEach(a => {
        const tarjeta = document.createElement('div');
        tarjeta.className = 'tarjeta-archivo';
        tarjeta.innerHTML = `
          <div class="info-archivo">
            <span class="nombre-archivo">${a.nombre}</span>
            <span class="meta-archivo">${a.registros} reservas | ${(a.tamano / 1024).toFixed(1)} KB</span>
          </div>
          <div class="acciones-archivo">
            <button class="btn-icono btn-ver" data-archivo="${a.nombre}" title="Previsualizar">👁</button>
            <a class="btn-icono" href="/api/descargar?archivo=${encodeURIComponent(a.nombre)}" title="Descargar" download>⬇</a>
          </div>
        `;
        rejillaArchivos.appendChild(tarjeta);
      });

      document.querySelectorAll('.btn-ver').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const nombre = e.currentTarget.getAttribute('data-archivo');
          mostrarModalArchivo(nombre);
        });
      });

    } catch (e) {
      logConsola('Error al consultar archivos: ' + e.message, 'error');
    }
  }

  async function mostrarModalArchivo(nombre) {
    try {
      const resp = await fetch(`/api/descargar?archivo=${encodeURIComponent(nombre)}`);
      const texto = await resp.text();
      modalTituloArchivo.textContent = nombre;
      modalCuerpoCsv.textContent = texto;
      btnDescargarCsvModal.href = `/api/descargar?archivo=${encodeURIComponent(nombre)}`;
      modalPrevisualizacion.classList.remove('oculta');
    } catch (e) {
      logConsola('Error al leer contenido del archivo: ' + e.message, 'error');
    }
  }

  async function generarDatosPrueba() {
    try {
      logConsola('Generando dataset de pruebas con los 3 casos de coincidencia...', 'aviso');
      const resp = await fetch('/api/generar-pruebas', { method: 'POST' });
      const datos = await resp.json();
      logConsola(`[GENERADO] ${datos.archivo} creado con éxito con caso de 5 personas coincidentes.`, 'exito');
    } catch (e) {
      logConsola('Error al generar pruebas: ' + e.message, 'error');
    }
  }

  function renderizarGruposCoincidentes(grupos) {
    if (!grupos || grupos.length === 0) {
      contenedorDetalleGrupos.classList.add('oculta');
      return;
    }
    contenedorDetalleGrupos.classList.remove('oculta');
    listaGruposCoincidentes.innerHTML = '';
    grupos.forEach(g => {
      const chip = document.createElement('div');
      chip.className = 'chip-grupo';
      chip.innerHTML = `<span class="chip-localizador">${g.localizador}</span><span>(${g.pasajeros} pasajeros)</span>`;
      listaGruposCoincidentes.appendChild(chip);
    });
  }

  function renderizarTablaReservas(reservas) {
    if (!reservas || reservas.length === 0) {
      cuerpoTablaReservas.innerHTML = '<tr><td colspan="6" class="celda-vacia">Sin reservas procesadas.</td></tr>';
      return;
    }
    cuerpoTablaReservas.innerHTML = '';
    reservas.forEach(r => agregarFilaTabla(r));
  }

  function agregarFilaTabla(r) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><code>${r.id || '-'}</code></td>
      <td><span class="${r.localizador && r.localizador !== 'N/A' ? 'badge-loc' : ''}">${r.localizador || 'N/A'}</span></td>
      <td><strong>${r.nombrePasajero}</strong></td>
      <td>${r.origen} ➔ ${r.destino}</td>
      <td><span class="badge-pais">${r.codigoPaisDestino}</span></td>
      <td>${r.fechaReserva}</td>
    `;
    cuerpoTablaReservas.appendChild(tr);
  }

  function actualizarBadgeEstado(tipo, texto) {
    badgeEstadoProceso.className = `badge-estado badge-${tipo}`;
    badgeEstadoProceso.textContent = texto;
  }

  function logConsola(mensaje, tipo = 'info') {
    const d = new Date();
    const hora = d.toTimeString().split(' ')[0];
    const linea = document.createElement('div');
    linea.className = `linea-consola ${tipo}`;
    linea.textContent = `[${hora}] ${mensaje}`;
    cuerpoConsola.appendChild(linea);
    cuerpoConsola.scrollTop = cuerpoConsola.scrollHeight;
  }

  cargarListaArchivos();
});
