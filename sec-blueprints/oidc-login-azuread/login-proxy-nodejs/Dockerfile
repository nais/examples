FROM navikt/node-express:12.2.0-alpine

RUN wget https://yarnpkg.com/install.sh | sh
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ARG NODE_ENV
ENV NODE_ENV $NODE_ENV

COPY . /usr/src/app/
EXPOSE 3000

ENTRYPOINT ["sh", "-c"]
CMD ["yarn start run"]
