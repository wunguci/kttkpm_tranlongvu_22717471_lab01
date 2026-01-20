FROM node:25-alpine
WORKDIR /app

COPY package.json .

RUN npm install

COPY producer_dlq.js .

EXPOSE 3000

CMD ["node", "producer_dlq.js"]