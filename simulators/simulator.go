package main

import (
	"encoding/json"
	"log"
	"math/rand"
	"time"

	"github.com/streadway/amqp"
)

const (
	heartbeatInterval   = 10 * time.Second
	consumptionInterval = 1 * time.Minute
	amqpURL             = "amqp://guest:guest@localhost:5672/"
)

func main() {
	conn, err := amqp.Dial(amqpURL)
	if err != nil {
		log.Fatalf("Failed to connect to RabbitMQ: %v", err)
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		log.Fatalf("Failed to open a channel: %v", err)
	}
	defer ch.Close()

	// Declare the queues
	_, err = ch.QueueDeclare("heartbeat_queue", true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare heartbeat queue: %v", err)
	}

	_, err = ch.QueueDeclare("consumption_queue", true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare consumption queue: %v", err)
	}

	go sendHeartbeat(ch)
	go sendConsumptionData(ch)

	select {}
}

func sendHeartbeat(ch *amqp.Channel) {
	ticker := time.NewTicker(heartbeatInterval)
	defer ticker.Stop()

	for range ticker.C {
		message := map[string]interface{}{
			"status":    "online",
			"id":        "simulator-1",
			"timestamp": time.Now().Format(time.RFC3339),
		}
		sendAMQPMessage(ch, "heartbeat_queue", message)
	}
}

func sendConsumptionData(ch *amqp.Channel) {
	ticker := time.NewTicker(consumptionInterval)
	defer ticker.Stop()

	for range ticker.C {
		consumption := rand.Float64() * 5.0
		message := map[string]interface{}{
			"id":          "simulator-1",
			"consumption": consumption,
			"timestamp":   time.Now().Format(time.RFC3339),
		}
		sendAMQPMessage(ch, "consumption_queue", message)
	}
}

func sendAMQPMessage(ch *amqp.Channel, queue string, data map[string]interface{}) {
	jsonData, err := json.Marshal(data)
	if err != nil {
		log.Printf("Error encoding JSON: %v", err)
		return
	}

	err = ch.Publish(
		"",    // Use default exchange to send directly to a queue
		queue, // Routing key is the queue name
		false, // Mandatory
		false, // Immediate
		amqp.Publishing{
			ContentType: "application/json",
			Body:        jsonData,
		},
	)
	if err != nil {
		log.Printf("Failed to publish message to %s: %v", queue, err)
	} else {
		log.Printf("Message sent to %s queue", queue)
	}
}
