FROM node:25-alpine
WORKDIR /app

COPY package.json .

RUN npm install

COPY consumer_dlq.js .

EXPOSE 3000

CMD ["node", "consumer_dlq.js"]