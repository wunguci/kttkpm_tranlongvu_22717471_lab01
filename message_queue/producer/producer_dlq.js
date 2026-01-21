const express = require('express');
const amqp = require('amqplib');

const app = express();
app.use(express.json());

const RABBITMQ_URL = 'amqp://user:password@rabbitmq:5672';
const QUEUE = 'email_queue';
const DEAD_LETTER_QUEUE = 'email_queue.dlq';

let channel;

async function connectRabbitMQ() {
    while (true) {
        try {
            const conn = await amqp.connect(RABBITMQ_URL);
            channel = await conn.createChannel();

            // Tạo queue với DLQ config
            await channel.assertQueue(QUEUE, {
                durable: true,
                deadLetterExchange: '',
                deadLetterRoutingKey: DEAD_LETTER_QUEUE
            });

            // Tạo DLQ để chứa failed messages
            await channel.assertQueue(DEAD_LETTER_QUEUE, {
                durable: true
            });

            console.log('Email Producer connected to RabbitMQ');
            break;
        } catch {
            console.log("Waiting for RabbitMQ...");
            await new Promise((r) => setTimeout(r, 3000));
        }
    }
}

app.post("/send-email", async (req, res) => {
    const { to, subject, body, type } = req.body;

    if (!to || !subject || !body) {
        return res.status(400).json({ error: "to, subject, and body are required" });
    }

    const emailData = {
        to,
        subject,
        body,
        type: type || 'general',
        queuedAt: new Date().toISOString()
    };

    channel.sendToQueue(
        QUEUE, 
        Buffer.from(JSON.stringify(emailData)),
        {persistent: true}
    );

    console.log("Email Sent:", emailData);

    res.json({
        status: "sent",
        email: emailData,
        message: "Email queued with DLQ support"
    });
});

connectRabbitMQ();

app.listen(3000, () => {
    console.log('Email Producer listening on port 3000');
});