# Quotes Frontend Application

This is an example project used to showcase functionality in the [Nais platform](https://nais.io). The application demonstrates how to build and deploy a modern web application with features like structured logging, in-memory data storage, and UUID-based routing.

## Features

- **Random Quote Display**: View a random quote on the homepage.
- **Submit New Quotes**: Add new quotes via a user-friendly form.
- **Shareable Links**: Each quote has a unique URL for sharing.
- **Structured Logging**: Uses `pino` for structured logging in both development and production environments.
- **In-Memory Data Storage**: Quotes are stored in a shared in-memory array.

## Tech Stack

- **Framework**: [Next.js](https://nextjs.org) for server-side rendering and routing.
- **Language**: TypeScript for type safety and modern JavaScript features.
- **Styling**: Tailwind CSS for utility-first styling.
- **Logging**: `pino` for structured and efficient logging.
- **UUIDs**: `uuid` library for generating unique identifiers.
- **API**: RESTful API routes built with Next.js.

## Getting Started

### Prerequisites

Ensure you have the following installed:

- [Node.js](https://nodejs.org/) (v22 or later)
- [yarn](https://yarnpkg.com/)

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/nais/examples/quotes-frontend.git
   cd quotes-frontend
   ```

2. Install dependencies:

   ```bash
   yarn install
   ```

### Running the Development Server

Start the development server:

```bash
yarn dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser to view the application.

### Building for Production

Build the application for production:

```bash
yarn build
```

Start the production server:

```bash
yarn start
```

### Testing

Run tests:

```bash
yarn test
```

## Environment Variables

Create a `.env.local` file in the root directory to configure environment variables:

```
NODE_ENV=development
PORT=3000
```

## Logging

The application uses `pino` for structured logging. Logs are formatted differently for development and production environments:

- **Development**: Pretty-printed logs with `pino-pretty`.
- **Production**: JSON-formatted logs.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
