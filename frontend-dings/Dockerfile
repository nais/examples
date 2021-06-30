FROM navikt/node-express:14-alpine

ARG NODE_ENV=production
ENV NODE_ENV=${NODE_ENV}
ENV TZ="Europe/Oslo"

WORKDIR /app

COPY node_modules/ node_modules/
COPY dist/server/ dist/server/
COPY dist/client/ dist/client/

EXPOSE 3000

ENTRYPOINT [ "/entrypoint.sh", "node /app/dist/server/index.js" ]
