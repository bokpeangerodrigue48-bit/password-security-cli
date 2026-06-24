FROM node:20-alpine
WORKDIR /app
COPY server.js .
RUN npm install zxcvbn
EXPOSE 3000
CMD ["node", "server.js"]