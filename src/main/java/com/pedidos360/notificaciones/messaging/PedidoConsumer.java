
package com.pedidos360.notificaciones.messaging;

import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoConsumer {

    private static final int MAX_INTENTOS = 3;

    @RabbitListener(
        queues = "${pedidos360.rabbitmq.queue}",
        ackMode = "MANUAL"
    )
    public void recibirPedido(Message message, Channel channel) throws Exception {

        long deliveryTag =
            message.getMessageProperties().getDeliveryTag();

        String contenido = new String(
            message.getBody(), StandardCharsets.UTF_8
        );

        System.out.println("Pedido recibido: " + contenido);

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try {
                procesarPedido(contenido);

                channel.basicAck(deliveryTag, false);
                System.out.println(
                    "ACK enviado. Procesado en intento " + intento
                );
                return;

            } catch (Exception e) {
                System.err.println(
                    "Intento " + intento + "/" + MAX_INTENTOS
                    + " fallido: " + e.getMessage()
                );

                if (intento == MAX_INTENTOS) {
                    channel.basicNack(deliveryTag, false, false);
                    System.err.println(
                        "NACK enviado. Mensaje dirigido a DLQ."
                    );
                    return;
                }
            }
        }
    }

    private void procesarPedido(String contenido) {
        if (contenido.contains("FORZAR_ERROR")) {
            throw new RuntimeException(
                "Error controlado para probar reintentos"
            );
        }

        System.out.println("Pedido procesado correctamente");
    }
}
