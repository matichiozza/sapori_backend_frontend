package com.example.demo.services;

import com.example.demo.datos.PagoDAO;
import com.example.demo.modelo.Pago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoAutomaticoService {

    @Autowired
    private PagoDAO pagoDAO;

    // Se ejecuta el día 28 de cada mes a las 23:59
    @Scheduled(cron = "0 59 23 28 * ?")
    @Transactional
    public void procesarPagosAutomaticos() {
        try {
            // Obtener todos los pagos pendientes
            List<Pago> pagosPendientes = pagoDAO.getPagosPorEstado(Pago.EstadoPago.A_PAGAR);
            
            for (Pago pago : pagosPendientes) {
                // Marcar como pagado
                pago.setEstado(Pago.EstadoPago.PAGO);
                pagoDAO.actualizarPago(pago);
                
                // Recalcular total de la cuenta corriente
                if (pago.getCuentaCorriente() != null) {
                    // Aquí podrías agregar lógica adicional si es necesario
                    // Por ejemplo, enviar notificación al alumno
                }
            }
            
            System.out.println("Pagos automáticos procesados: " + pagosPendientes.size() + " pagos marcados como pagados.");
            
        } catch (Exception e) {
            System.err.println("Error al procesar pagos automáticos: " + e.getMessage());
        }
    }
} 