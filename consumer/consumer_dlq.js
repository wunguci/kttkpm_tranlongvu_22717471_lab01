const amqp = require("amqplib");

const RABBITMQ_URL = "amqp://user:password@rabbitmq:5672";
const QUEUE = "email_queue";
const DEAD_LETTER_QUEUE = "email_queue.dlq";

let channel;

async function connectWithRetry() {
    try {
        console.log("Email Worker (DLQ) connecting...");
        const conn = await amqp.connect(RABBITMQ_URL);
        channel = await conn.createChannel();

        // Tạo DLQ trước
        await channel.assertQueue(DEAD_LETTER_QUEUE, {
            durable: true,
        });

        // Tạo main queue vứi DLQ config
        await channel.assertQueue(QUEUE, {
            durable: true,
            deadLetterExchange: "",
            deadLetterRoutingKey: DEAD_LETTER_QUEUE,
        });

        channel.prefetch(1);

        console.log("Email Worker ready with DLQ support");
        console.log("Waiting for emails...\n");

        channel.consume(QUEUE, async (msg) => {
            if (!msg) return;

            const email = JSON.parse(msg.content.toString());

            console.log("========================================");
            console.log("Processing email");
            console.log("To:", email.to);
            console.log("Subject:", email.subject);
            console.log("Type:", email.type);

            try {
                // Validation: check email format
                if (!email.to.includes("@")) {
                    throw new Error("Invalid email format");
                }

                // Validation: check if email is blocked
                const blockedDomains = ["spam.com", "blocked.com"];
                const domain = email.to.split("@")[1];
                if (blockedDomains.includes(domain)) {
                    throw new Error(`Domain ${domain} is blocked`);
                }

                // mô phỏng gửi email
                console.log("Connecting to SMTP...");
                await new Promise((resolve) => setTimeout(resolve, 1000));

                console.log("Sending email...");
                await new Promise((resolve) => setTimeout(resolve, 2000));

                console.log("Email sent successfully!");
                console.log("Sent at:", new Date().toISOString());
                console.log("========================================\n");

                channel.ack(msg);
            
            } catch (error) {
                console.log("ERROR:", error.message);
                console.log("Moving to Dead Letter Queue");
                console.log("========================================\n");

                // Rejeact và không requeue => message đi vào DLQ
                channel.nack(msg, false, false);
            }
        });
    } catch (err) {
        console.log("Email Worker retry in 3s...");
        setTimeout(connectWithRetry, 3000);
    }
}

connectWithRetry();