// Avatar con iniciales — Agenda Tutorías
function generarAvatar(nombre, elemento, tamaño) {
    if (!nombre || !elemento) return;

    const colores = [
        '#1a3a5c', '#24527a', '#c0392b', '#1e8449',
        '#d35400', '#7d3c98', '#1a5276', '#117a65'
    ];

    const iniciales = nombre.trim().split(' ')
        .filter(p => p.length > 0)
        .slice(0, 2)
        .map(p => p[0].toUpperCase())
        .join('');

    const indice = nombre.charCodeAt(0) % colores.length;
    const color = colores[indice];

    elemento.style.background = color;
    elemento.textContent = iniciales;

    if (tamaño === 'grande') elemento.classList.add('grande');
    if (tamaño === 'pequeno') elemento.classList.add('pequeno');
}

// Inicializar todos los avatares de la página
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-avatar]').forEach(function (el) {
        const nombre = el.getAttribute('data-avatar');
        const tamaño = el.getAttribute('data-avatar-size') || '';
        generarAvatar(nombre, el, tamaño);
    });
});