FROM cgr.dev/chainguard/node:20
# TODO change to match the path to your code
WORKDIR /app
COPY --chown=node:node ./mystuff.js .
WORKDIR /app
CMD ["mystuff.js"]
