package com.pedidos360.notificaciones;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @GetMapping("/estado")
    public Map<String, String> obtenerEstado() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("servicio", "notificaciones");
        respuesta.put("estado", "activo");
        return respuesta;
    }
}