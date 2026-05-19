package com.example.agenda_tutoria;

import com.example.agenda_tutoria.model.HistorialEstado;
import com.example.agenda_tutoria.model.Pago;
import com.example.agenda_tutoria.model.Tutoria;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.HistorialEstadoRepository;
import com.example.agenda_tutoria.repository.PagoRepository;
import com.example.agenda_tutoria.repository.TutoriaRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private TutoriaRepository tutoriaRepository;
    @Autowired private HistorialEstadoRepository historialEstadoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String[] MATERIAS = {
        "Programación I", "Programación II", "Desarrollo Web", "Spring Boot",
        "Cálculo I", "Cálculo II", "Álgebra Lineal", "Estadística",
        "Bases de Datos I", "Bases de Datos II", "MongoDB", "SQL",
        "Inglés I", "Inglés II", "Inglés Técnico", "Conversación",
        "Redes I", "Redes II", "Packet Tracer", "Seguridad en Redes",
        "Ingeniería de Software", "UML", "Patrones de Diseño", "Scrum",
        "Física I", "Física II", "Electrónica Básica", "Circuitos",
        "Contabilidad I", "Contabilidad II", "Finanzas", "Excel Avanzado"
    };

    private static final String[] TEMAS = {
        "Introducción", "Ejercicios prácticos", "Proyecto final",
        "Repaso de conceptos", "Solución de dudas", "Taller dirigido",
        "Preparación de examen", "Caso de estudio"
    };

    private static final Tutoria.Estado[] ESTADOS = Tutoria.Estado.values();
    private static final Random RND = new Random(42);

    @Override
    public void run(String... args) {
        // Admin
        crearSiNoExiste("admin@tutorias.com", "Administrador", "admin123",
            "ADMIN", null, null, null, null, 0, true);

        // Tutores
        crearSiNoExiste("carlos.perez@tutorias.com", "Carlos Andrés Pérez",
            "tutor123", "PROFESOR", "Programación y Desarrollo Web",
            "Desarrollador con 6 años de experiencia en Java, Spring Boot y React.",
            Arrays.asList("Programación I", "Programación II", "Desarrollo Web", "Spring Boot"),
            45000.0, 6, true);

        crearSiNoExiste("maria.gonzalez@tutorias.com", "María Fernanda González",
            "tutor123", "PROFESOR", "Matemáticas y Cálculo",
            "Ingeniera matemática con maestría en educación.",
            Arrays.asList("Cálculo I", "Cálculo II", "Álgebra Lineal", "Estadística"),
            35000.0, 4, true);

        crearSiNoExiste("juan.martinez@tutorias.com", "Juan David Martínez",
            "tutor123", "PROFESOR", "Bases de Datos",
            "Experto en diseño y administración de bases de datos relacionales y NoSQL.",
            Arrays.asList("Bases de Datos I", "Bases de Datos II", "MongoDB", "SQL"),
            40000.0, 5, true);

        crearSiNoExiste("ana.rodriguez@tutorias.com", "Ana Sofía Rodríguez",
            "tutor123", "PROFESOR", "Inglés Técnico",
            "Profesora de inglés certificada por Cambridge.",
            Arrays.asList("Inglés I", "Inglés II", "Inglés Técnico", "Conversación"),
            30000.0, 7, true);

        crearSiNoExiste("david.torres@tutorias.com", "David Esteban Torres",
            "tutor123", "PROFESOR", "Redes y Sistemas",
            "Técnico en redes con certificación CCNA.",
            Arrays.asList("Redes I", "Redes II", "Packet Tracer", "Seguridad en Redes"),
            38000.0, 3, true);

        crearSiNoExiste("laura.jimenez@tutorias.com", "Laura Valentina Jiménez",
            "tutor123", "PROFESOR", "Diseño de Software",
            "Ingeniera de software con experiencia en metodologías ágiles.",
            Arrays.asList("Ingeniería de Software", "UML", "Patrones de Diseño", "Scrum"),
            42000.0, 5, true);

        crearSiNoExiste("miguel.vargas@tutorias.com", "Miguel Ángel Vargas",
            "tutor123", "PROFESOR", "Física y Electrónica",
            "Ingeniero electrónico con especialización en física aplicada.",
            Arrays.asList("Física I", "Física II", "Electrónica Básica", "Circuitos"),
            36000.0, 8, true);

        crearSiNoExiste("camila.herrera@tutorias.com", "Camila Andrea Herrera",
            "tutor123", "PROFESOR", "Contabilidad y Finanzas",
            "Contadora pública con experiencia en contabilidad empresarial.",
            Arrays.asList("Contabilidad I", "Contabilidad II", "Finanzas", "Excel Avanzado"),
            32000.0, 4, true);

        // Estudiante de prueba
        crearSiNoExiste("estudiante@tutorias.com", "María García",
            "est123", "ESTUDIANTE", null, null, null, null, 0, true);

        // Estudiantes adicionales para datos de prueba
        String[][] estudiantesExtra = {
            {"andres.lopez@tutorias.com", "Andrés López", "est123"},
            {"carmen.diaz@tutorias.com", "Carmen Díaz", "est123"},
            {"jorge.ramirez@tutorias.com", "Jorge Ramírez", "est123"},
            {"lucia.morales@tutorias.com", "Lucía Morales", "est123"},
            {"fernando.castro@tutorias.com", "Fernando Castro", "est123"},
            {"patricia.ortiz@tutorias.com", "Patricia Ortiz", "est123"},
            {"ricardo.silva@tutorias.com", "Ricardo Silva", "est123"},
            {"veronica.mendoza@tutorias.com", "Verónica Mendoza", "est123"},
            {"diego.rojas@tutorias.com", "Diego Rojas", "est123"},
            {"gabriela.navarro@tutorias.com", "Gabriela Navarro", "est123"},
            {"sergio.paredes@tutorias.com", "Sergio Paredes", "est123"},
            {"alejandra.molina@tutorias.com", "Alejandra Molina", "est123"}
        };
        for (String[] e : estudiantesExtra) {
            crearSiNoExiste(e[0], e[1], e[2], "ESTUDIANTE", null, null, null, null, 0, true);
        }

        // Tutores adicionales para datos de prueba
        String[][] tutoresExtra = {
            {"andres.vega@tutorias.com", "Andrés Vega", "Machine Learning", "Especialista en IA y ML.", "Machine Learning, Deep Learning, Python Avanzado", "50000", "6"},
            {"carolina.delgado@tutorias.com", "Carolina Delgado", "Diseño Gráfico", "Diseñadora UX/UI.", "Diseño Gráfico, Illustrator, Photoshop", "28000", "8"},
            {"felipe.montoya@tutorias.com", "Felipe Montoya", "Marketing Digital", "Experto en SEO y SEM.", "Marketing Digital, SEO, Google Ads", "32000", "5"},
            {"valentina.salazar@tutorias.com", "Valentina Salazar", "Derecho", "Abogada comercial.", "Derecho Comercial, Derecho Laboral", "40000", "7"},
            {"sebastian.moreno@tutorias.com", "Sebastián Moreno", "Análisis de Datos", "Data analyst.", "Análisis de Datos, Python, Power BI", "45000", "4"},
            {"daniela.cordoba@tutorias.com", "Daniela Córdoba", "Arquitectura", "Arquitecta sostenible.", "Arquitectura, AutoCAD, Diseño Sostenible", "35000", "6"},
            {"javier.medina@tutorias.com", "Javier Medina", "Electricidad", "Ingeniero electricista.", "Electricidad, Circuitos, Instalaciones", "30000", "10"},
            {"mariana.roa@tutorias.com", "Mariana Roa", "Inteligencia Artificial", "ML Engineer.", "IA, TensorFlow, NLP", "55000", "5"},
            {"camilo.arias@tutorias.com", "Camilo Arias", "Blockchain", "Desarrollador Web3.", "Blockchain, Solidity, Ethereum", "48000", "4"},
            {"tatiana.mora@tutorias.com", "Tatiana Mora", "Estadística", "Estadística aplicada.", "Estadística, R, SPSS", "33000", "7"},
            {"henry.gomez@tutorias.com", "Henry Gómez", "Robótica", "Ingeniero en robótica.", "Robótica, Arduino, Automatización", "42000", "6"},
            {"paola.rangel@tutorias.com", "Paola Rangel", "Química", "Química pura.", "Química General, Orgánica, Laboratorio", "31000", "5"},
            {"leonardo.pena@tutorias.com", "Leonardo Peña", "Filosofía", "Filósofo y escritor.", "Filosofía, Ética, Lógica", "25000", "9"},
            {"giselle.cardenas@tutorias.com", "Giselle Cárdenas", "Biología", "Bióloga molecular.", "Biología, Genética, Ecología", "29000", "4"},
            {"brayan.escobar@tutorias.com", "Brayan Escobar", "Ciberseguridad", "Hacker ético.", "Ciberseguridad, Ethical Hacking, Linux", "47000", "5"},
            {"jennifer.ospina@tutorias.com", "Jennifer Ospina", "Psicología", "Psicóloga clínica.", "Psicología General, Clínica, Social", "27000", "8"},
            {"mauricio.beltran@tutorias.com", "Mauricio Beltrán", "Administración", "MBA y consultor.", "Administración, RRHH, Emprendimiento", "34000", "12"},
            {"ximena.barrera@tutorias.com", "Ximena Barrera", "Nutrición", "Nutricionista deportiva.", "Nutrición, Dietética, Salud", "26000", "5"},
            {"rafael.maldonado@tutorias.com", "Rafael Maldonado", "Big Data", "Data engineer.", "Big Data, Hadoop, Spark", "52000", "6"},
            {"katherine.uribe@tutorias.com", "Katherine Uribe", "Idiomas", "Políglota (5 idiomas).", "Inglés, Francés, Alemán, Portugués", "22000", "10"},
            {"augusto.santana@tutorias.com", "Augusto Santana", "Geografía", "Geógrafo SIG.", "Geografía, SIG, Cartografía", "28000", "4"},
            {"isabel.castillo@tutorias.com", "Isabel Castillo", "Literatura", "Escritora y correctora.", "Literatura, Gramática, Redacción", "24000", "7"},
            {"ernesto.garzon@tutorias.com", "Ernesto Garzón", "Física Cuántica", "Físico teórico.", "Física Cuántica, Termodinámica, Óptica", "46000", "6"},
            {"lorena.quintero@tutorias.com", "Lorena Quintero", "Pedagogía", "Docente universitaria.", "Pedagogía, Didáctica, Evaluación", "30000", "9"},
            {"emanuel.duarte@tutorias.com", "Emanuel Duarte", "Python", "Python developer.", "Python, Django, FastAPI", "43000", "5"},
            {"adriana.mejia@tutorias.com", "Adriana Mejía", "Economía", "Econometrista.", "Microeconomía, Macroeconomía, Econometría", "36000", "6"},
            {"roman.pacheco@tutorias.com", "Román Pacheco", "DevOps", "SRE engineer.", "Docker, Kubernetes, CI/CD", "54000", "7"},
            {"vanessa.lozano@tutorias.com", "Vanessa Lozano", "Sociología", "Socióloga investigadora.", "Sociología, Antropología, Metodología", "25000", "5"},
            {"humberto.galindo@tutorias.com", "Humberto Galindo", "Excel Avanzado", "Especialista en Excel.", "Excel, VBA, Power Query", "28000", "11"},
            {"aleida.bernal@tutorias.com", "Aleida Bernal", "Medicina", "Médica general.", "Anatomía, Fisiología, Farmacología", "45000", "8"}
        };
        for (String[] t : tutoresExtra) {
            String nombre = t[1];
            String especialidad = t[2];
            String descripcion = t[3];
            List<String> materias = Arrays.asList(t[4].split(",\\s*"));
            double precio = Double.parseDouble(t[5]);
            int experiencia = Integer.parseInt(t[6]);
            crearSiNoExiste(t[0], nombre, "tutor123", "PROFESOR", especialidad, descripcion, materias, precio, experiencia, true);
        }

        List<Usuario> estudiantes = usuarioRepository.findByRol("ESTUDIANTE");
        List<Usuario> profesores = usuarioRepository.findByRol("PROFESOR");

        if (!estudiantes.isEmpty() && !profesores.isEmpty()) {
            // Seed pagos (solo si la tabla está vacía)
            if (pagoRepository.count() == 0) {
                List<Pago> pagos = new ArrayList<>();
                for (int i = 0; i < Math.min(10, estudiantes.size()); i++) {
                    Usuario est = estudiantes.get(i);
                    pagos.add(new Pago(est.getId(), est.getNombre(), Pago.Tipo.RECARGA, 50000.0,
                            "Bono de bienvenida al registrarte"));
                }
                pagoRepository.saveAll(pagos);
                System.out.println("✅ Pagos seed — " + pagos.size() + " registros");
            }

            // Seed tutorías masivas — siempre reseedear si hay menos de 100
            // o si detectamos materias mal asignadas (de una versión anterior)
            boolean reseedNeeded = false;
            long totalTutorias = tutoriaRepository.count();
            if (totalTutorias >= 100) {
                // Revisar TODAS las tutorías en lotes de 500 hasta encontrar una mal
                int page = 0;
                int pageSize = 500;
                while (true) {
                    List<Tutoria> batch = tutoriaRepository.findAll(
                            PageRequest.of(page, pageSize)).getContent();
                    if (batch.isEmpty()) break;
                    for (Tutoria t : batch) {
                        Usuario profe = profesores.stream()
                                .filter(p -> p.getId().equals(t.getProfesorId()))
                                .findFirst().orElse(null);
                        if (profe != null && profe.getMaterias() != null && !profe.getMaterias().isEmpty()) {
                            if (!profe.getMaterias().contains(t.getMateria())) {
                                reseedNeeded = true;
                                break;
                            }
                        }
                    }
                    if (reseedNeeded) break;
                    page++;
                }
            }
            // Siempre reseedear si hay tutorías pero son menos de 15,000
            // (cubre casos de seed parcial por reinicio de Railway)
            if (totalTutorias < 100 || reseedNeeded || (totalTutorias >= 100 && totalTutorias < 5000)) {
                System.out.println("🗑️ Limpiando datos antiguos para reseed...");
                historialEstadoRepository.deleteAll();
                pagoRepository.deleteAll();
                tutoriaRepository.deleteAll();
                System.out.println("✅ Bases limpias — generando 15,000 tutorías...");
                seedMassiveData(estudiantes, profesores);
            }
        }

        System.out.println("✅ DataLoader completado — " +
            usuarioRepository.count() + " usuarios, " +
            tutoriaRepository.count() + " tutorías, " +
            pagoRepository.count() + " pagos");
    }

    private void seedMassiveData(List<Usuario> estudiantes, List<Usuario> profesores) {
        int target = 5000;
        int batchSize = 100;
        List<Tutoria> tutorias = new ArrayList<>(batchSize);
        List<Pago> pagos = new ArrayList<>(batchSize);
        List<HistorialEstado> historiales = new ArrayList<>(batchSize);

        for (int i = 0; i < target; i++) {
            Usuario est = estudiantes.get(RND.nextInt(estudiantes.size()));
            Usuario prof = profesores.get(RND.nextInt(profesores.size()));
            Tutoria.Estado estado = ESTADOS[RND.nextInt(ESTADOS.length)];
            boolean pagada = estado == Tutoria.Estado.PAGADA || estado == Tutoria.Estado.EN_REVISION;

            Tutoria t = new Tutoria();
            t.setEstudianteId(est.getId());
            t.setEstudianteNombre(est.getNombre());
            t.setProfesorId(prof.getId());
            t.setProfesorNombre(prof.getNombre());
            t.setMateria(prof.getMaterias() != null && !prof.getMaterias().isEmpty()
                    ? prof.getMaterias().get(RND.nextInt(prof.getMaterias().size()))
                    : MATERIAS[RND.nextInt(MATERIAS.length)]);
            t.setTema(TEMAS[RND.nextInt(TEMAS.length)]);
            t.setFechaHora(LocalDateTime.now().minusDays(RND.nextInt(90)).plusHours(RND.nextInt(24)));
            t.setDuracionMin(List.of(30, 60, 90, 120).get(RND.nextInt(4)));
            t.setPrecioTotal(prof.getPrecioPorHora() != null ? prof.getPrecioPorHora() * (RND.nextInt(3) + 1) : 35000.0);
            t.setEstado(estado);
            t.setPagado(pagada);
            tutorias.add(t);

            if (pagada) {
                Pago p = new Pago(prof.getId(), prof.getNombre(),
                        Pago.Tipo.INGRESO, t.getPrecioTotal() * 0.90,
                        "Pago por tutoría #" + i + " con " + est.getNombre());
                pagos.add(p);
            }

            HistorialEstado h = new HistorialEstado();
            h.setTutoriaId(t.getId());
            h.setEstadoAnterior("CREADA");
            h.setEstadoNuevo(estado.name());
            h.setCambiadoPorNombre("DataLoader");
            h.setRol("SISTEMA");
            historiales.add(h);

            if ((i + 1) % batchSize == 0 || i == target - 1) {
                tutoriaRepository.saveAll(tutorias);
                if (!pagos.isEmpty()) pagoRepository.saveAll(pagos);
                historialEstadoRepository.saveAll(historiales);
                tutorias.clear();
                pagos.clear();
                historiales.clear();
                if ((i + 1) % 3000 == 0) {
                    System.out.println("📦 " + (i + 1) + "/" + target + " tutorías insertadas...");
                }
            }
        }
        System.out.println("✅ Seed masivo completado — " + target + " tutorías");
    }

    private void crearSiNoExiste(String correo, String nombre, String password,
                                  String rol, String especialidad, String descripcion,
                                  List<String> materias, Double precio,
                                  int experiencia, boolean verificado) {
        // Si ya existe, solo asegurar que esté verificada, tenga balance y materias correctas
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            Usuario existente = usuarioRepository.findByCorreo(correo).get();
            boolean cambio = false;
            if (verificado && Boolean.FALSE.equals(existente.getCuentaVerificada())) {
                existente.setCuentaVerificada(true);
                cambio = true;
            }
            if (existente.getBalance() == null || existente.getBalance() == 0.0) {
                existente.setBalance("PROFESOR".equals(rol) ? 0.0 : 50000.0);
                cambio = true;
            }
            // Actualizar materias aunque el usuario ya exista (previene datos huérfanos de versiones anteriores)
            if (materias != null && !materias.isEmpty()
                    && (existente.getMaterias() == null || !existente.getMaterias().equals(materias))) {
                existente.setMaterias(materias);
                cambio = true;
            }
            if (precio != null && existente.getPrecioPorHora() == null) {
                existente.setPrecioPorHora(precio);
                cambio = true;
            }
            if (cambio) {
                usuarioRepository.save(existente);
            }
            return;
        }

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setCuentaVerificada(verificado);
        u.setBalance("PROFESOR".equals(rol) ? 0.0 : 50000.0);
        u.setCalificacionPromedio(3.5 + Math.random() * 1.5);
        u.setTotalCalificaciones((int)(Math.random() * 50) + 5);

        if (especialidad != null) u.setEspecialidad(especialidad);
        if (descripcion != null)  u.setDescripcion(descripcion);
        if (materias != null)     u.setMaterias(materias);
        if (precio != null)       u.setPrecioPorHora(precio);
        if (experiencia > 0)      u.setAniosExperiencia(experiencia);

        usuarioRepository.save(u);
        System.out.println("✅ Creado: " + nombre + " (" + rol + ")");
    }
}
