const express = require('express');
const amqp = require('amqplib');

const app = express();
app.use(express.json());

const RABBITMQ_URL = "amqp://user:password@rabbitmq:5672";
const QUEUE = "email_queue";

let channel;

// Hàm kết nối đến RabbitMQ với cơ chế retry
async function connectRabbitMQ() {
    while (true) {
        try {
            // Kết nối đến RabbitMQ
            const conn = await amqp.connect(RABBITMQ_URL);
            // Tạo kênh
            channel = await conn.createChannel();
            // Khai báo hàng đợi
            await channel.assertQueue(QUEUE, {durable: true});

            console.log("Email Producer connected to RabbitMQ");
            break;
        } catch (error) {
            console.log("Waiting for RabbitMQ...");
            await new Promise((r) => setTimeout(r, 3000));
        }
    }
}

// Endpoint để gửi email
app.post('/send-email', async (req, res) => {
    const {to, subject, body, type} = req.body;

    if (!to || !subject || !body || !type) {
        return res.status(400).json({
            error: "to, subject, body, and type are required"
        });
    }

    const emailData = {
        to,
        subject,
        body,
        type: type || "general",
        queuedAt: new Date().toISOString()
    };

    // Gửi message vào hàng đợi
    channel.sendToQueue(
        QUEUE,
        Buffer.from(JSON.stringify(emailData)), // Chuyển đổi đối tượng emailData thành Buffer
        {
            persistent: true // Đảm bảo message không bị mất khi RabbitMQ restart
        }
    );

    console.log("Email sent:", subject);

    res.json({
        status: "sent",
        email: emailData,
        message: "Email has been queued for sending"
    });
});

connectRabbitMQ();

app.listen(3000, () => {
    console.log("Email Producer API listening on port 3000");
});