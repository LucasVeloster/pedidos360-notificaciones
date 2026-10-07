package com.pedidos360.notificaciones.messaging;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoConsumer {

    @RabbitListener(
            queues = "${pedidos360.rabbitmq.queue}",
            ackMode = "MANUAL"
    )
    public void recibirPedido(Message message, Channel channel) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            String contenido = new String(message.getBody());

            System.out.println("Pedido recibido desde RabbitMQ:");
            System.out.println(contenido);

            if (contenido.contains("FORZAR_ERROR")) {
                throw new RuntimeException("Error controlado para probar DLQ");
            }

            channel.basicAck(deliveryTag, false);

            System.out.println("ACK enviado correctamente");

        } catch (Exception e) {

            System.err.println("Error procesando mensaje: " + e.getMessage());

            channel.basicNack(deliveryTag, false, false);

            System.err.println("NACK enviado. Mensaje enviado a DLQ.");
        }
    }
}
