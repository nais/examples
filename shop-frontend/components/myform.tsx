import React, { useState } from "react";
import { Button, Heading, Textarea, TextField } from "@navikt/ds-react";
//import { postFeedback } from "../lib/api";

const MyForm = () => {
  const [name, setName] = useState("");
  const [feedback, setFeedback] = useState("");

  const handleSubmit = async () => {
    //const response = await postFeedback({ name, feedback });
    //console.log(response);
  };

  return (
    <div>
      <Heading spacing level="2" size="medium">
        Simple form
      </Heading>
      <main>
        <div className="my-form">
          <TextField htmlSize={50} label="Name" value={name} onChange={(e) => setName(e.target.value)} />
          <Textarea label="Feedback" value={feedback} onChange={(e) => setFeedback(e.target.value)} />
          <Button onClick={handleSubmit}>Submit</Button>
        </div>
      </main>
    </div>
  );
};

export default MyForm;