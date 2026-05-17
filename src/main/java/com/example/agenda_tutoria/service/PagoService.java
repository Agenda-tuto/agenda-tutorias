package com.example.agenda_tutoria.service;

import com.example.agenda_tutoria.model.HistorialEstado;
import com.example.agenda_tutoria.model.Pago;
import com.example.agenda_tutoria.model.Transaccion;
import com.example.agenda_tutoria.model.Usuario;
import com.example.agenda_tutoria.repository.HistorialEstadoRepository;
import com.example.agenda_tutoria.repository.PagoRepository;
import com.example.agenda_tutoria.repository.TransaccionRepository;
import com.example.agenda_tutoria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PagoService {

    private static final double COMISION = 0.10;
    private static final String PLATAFORMA_ID = "000000000000000000000000";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private HistorialEstadoRepository historialEstadoRepository;

    public void recargar(Usuario usuario, Double monto) {
        usuario.setBalance(usuario.getBalance() + monto);
        usuarioRepository.save(usuario);

        Pago pago = new Pago(
            usuario.getId(), usuario.getNombre(),
            Pago.Tipo.RECARGA, monto,
            "Recarga de billetera por $" + String.format("%,.0f", monto)
        );
        pagoRepository.save(pago);
    }

    public boolean tieneSaldo(Usuario estudiante, Double monto) {
        return estudiante.getBalance() != null && estudiante.getBalance() >= monto;
    }

    public Double calcularCosto(Double precioPorHora, Integer duracionMin) {
        if (precioPorHora == null || duracionMin == null) return 0.0;
        return precioPorHora * duracionMin / 60.0;
    }

    public boolean procesarPago(Usuario estudiante, Usuario tutor,
                                 Double monto, String tutoriaId) {
        if (!tieneSaldo(estudiante, monto)) return false;

        double comision = monto * COMISION;
        double gananciaTutor = monto - comision;

        estudiante.setBalance(estudiante.getBalance() - monto);
        usuarioRepository.save(estudiante);

        double balanceTutor = tutor.getBalance() != null ? tutor.getBalance() : 0.0;
        tutor.setBalance(balanceTutor + gananciaTutor);
        usuarioRepository.save(tutor);

        Pago pagoEst = new Pago(
            estudiante.getId(), estudiante.getNombre(),
            Pago.Tipo.PAGO, monto,
            "Pago por tutoría con " + tutor.getNombre()
        );
        pagoEst.setReferenciaId(tutoriaId);
        pagoRepository.save(pagoEst);

        Pago ingresoTutor = new Pago(
            tutor.getId(), tutor.getNombre(),
            Pago.Tipo.INGRESO, gananciaTutor,
            "Ingreso por tutoría con " + estudiante.getNombre()
                + " (comisión 10% descontada)"
        );
        ingresoTutor.setReferenciaId(tutoriaId);
        pagoRepository.save(ingresoTutor);

        Pago comisionPago = new Pago(
            PLATAFORMA_ID, "Agenda Tutorías",
            Pago.Tipo.COMISION, comision,
            "Comisión 10% de tutoría entre "
                + estudiante.getNombre() + " y " + tutor.getNombre()
        );
        comisionPago.setReferenciaId(tutoriaId);
        pagoRepository.save(comisionPago);

        return true;
    }

    public void registrarCambioEstado(String tutoriaId, String estadoAnterior, String estadoNuevo,
                                       String cambiadoPorId, String cambiadoPorNombre, String rol) {
        historialEstadoRepository.save(new HistorialEstado(
            tutoriaId, estadoAnterior, estadoNuevo,
            cambiadoPorId, cambiadoPorNombre, rol
        ));
    }
}