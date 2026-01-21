const amqp = require("amqplib");

const RABBITMQ_URL = "amqp://user:password@rabbitmq:5672";
const QUEUE = "email_queue";

async function connectWithRetry() {
    try {
        console.log("Email Worker connecting...");
        // Khai báo kết nối đến RabbitMQ
        const conn = await amqp.connect(RABBITMQ_URL);
        // Tạo kênh giao tiếp
        const channel = await conn.createChannel();

        // Đảm bảo hàng đợi tồn tại
        await channel.assertQueue(QUEUE, {durable: true});

        // Lắng nghe và xử lý tin nhắn từ hàng đợi
        channel.prefetch(1);

        console.log("Waiting for email messages...");

        // Xử lý tin nhắn
        channel.consume(QUEUE, async (msg) => {
            if (msg) {
                const email = JSON.parse(msg.content.toString());

                console.log("==============================");
                console.log("Processing email");
                console.log("To:", email.to);
                console.log("Subject:", email.subject);
                console.log("Type:", email.type);
                console.log("Queued at:", email.queuedAt);

                // Mô phỏng việc gửi email
                console.log("Connecting to SMTP server...");
                await new Promise(resolve => setTimeout(resolve, 1000));

                console.log("Sending email...");
                await new Promise(resolve => setTimeout(resolve, 2000));

                console.log("Email sent successfully to", email.to);
                console.log("Sent at:", new Date().toISOString());

                // Xác nhận đã xử lý tin nhắn
                channel.ack(msg);
            }
        });;
    } catch (err) {
        console.log("Email Consumer retry in 3s...");
        setTimeout(connectWithRetry, 3000);
    }
}

connectWithRetry();