# SpringBoot Mistral POC

## Description
This project is a proof of concept for integrating the Mistral AI model with a Spring Boot application. It demonstrates how to set up a REST API that interacts with the Mistral model to generate text based on user input.

## Requirements
### Podman
use ``docker-compose.yml`` with ```podman-compose up```.
this will create a OLLAMA container. <br>
```podman exec -it mistral-ollama ollama pull mistral``` will pull the mistral model.

You can check the successful installation by running the following curl command:
```bash
curl http://localhost:11434/api/chat -d '{
  "model": "mistral",
  "stream": false,
  "messages": [{"role": "user", "content": "Hey, how are you?"}]
}'

```

