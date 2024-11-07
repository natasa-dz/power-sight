package main

import (
	"encoding/json"
	"flag"
	"log"
	"math"
	"math/rand"
	"strings"
	"time"

	"github.com/streadway/amqp"
)

const (
	heartbeatInterval   = 15 * time.Second
	consumptionInterval = 1 * time.Minute
	amqpURL             = "amqp://guest:guest@localhost:5672/"
)

func GetHourlyConsumption(hour int) float64 {
	baseConsumption := 0.35
	switch {
	case hour >= 0 && hour < 6:
		return baseConsumption * 0.5
	case hour >= 6 && hour < 9:
		return baseConsumption * 3.0
	case hour >= 9 && hour < 18:
		return baseConsumption * 1.5
	case hour >= 18 && hour < 22:
		return baseConsumption * 4.0
	default:
		return baseConsumption * 2.0
	}
}

var lastSuccessfulMessageTime string

func main() {
	id := flag.String("id", "simulator", "Unique ID for the simulator instance")
	municipalityInput := flag.String("municipality", "novisad", "Municipality name")
	flag.Parse()

	municipality := strings.ToLower(strings.ReplaceAll(*municipalityInput, " ", ""))

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

	// Declare exchanges for heartbeat and consumption
	err = ch.ExchangeDeclare("heartbeatExchange", "direct", true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare heartbeat exchange: %v", err)
	}
	err = ch.ExchangeDeclare("consumptionExchange", "direct", true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare consumption exchange: %v", err)
	}

	// Declare and bind the queues to their respective exchanges
	heartbeatQueueName := "heartbeat_queue_" + municipality
	consumptionQueueName := "consumption_queue_" + municipality

	_, err = ch.QueueDeclare(heartbeatQueueName, true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare heartbeat queue: %v", err)
	}
	err = ch.QueueBind(heartbeatQueueName, heartbeatQueueName, "heartbeatExchange", false, nil)
	if err != nil {
		log.Fatalf("Failed to bind heartbeat queue: %v", err)
	}

	_, err = ch.QueueDeclare(consumptionQueueName, true, false, false, false, nil)
	if err != nil {
		log.Fatalf("Failed to declare consumption queue: %v", err)
	}
	err = ch.QueueBind(consumptionQueueName, consumptionQueueName, "consumptionExchange", false, nil)
	if err != nil {
		log.Fatalf("Failed to bind consumption queue: %v", err)
	}

	go sendHeartbeat(ch, *id, heartbeatQueueName)
	go sendConsumptionData(ch, *id, consumptionQueueName)

	select {}
}

func sendHeartbeat(ch *amqp.Channel, id string, queue string) {
	ticker := time.NewTicker(heartbeatInterval)
	defer ticker.Stop()

	for range ticker.C {
		message := map[string]interface{}{
			"status":    "online",
			"id":        "simulator-" + id,
			"timestamp": time.Now().Format(time.RFC3339),
		}
		sendAMQPMessage(ch, "heartbeatExchange", queue, message)
	}
}

func sendConsumptionData(ch *amqp.Channel, id string, queue string) {
	ticker := time.NewTicker(consumptionInterval)
	defer ticker.Stop()

	for range ticker.C {
		baseDate := time.Date(2024, time.November, 1, 0, 0, 0, 0, time.UTC)
		now := time.Now().UTC()
		timePassed := now.Sub(baseDate)
		simulationHoursPassed := int(math.Round(timePassed.Minutes())) + 60
		daysPassed := simulationHoursPassed / 24
		hour := simulationHoursPassed % 24
		date := baseDate.AddDate(0, 0, daysPassed)
		simulationTime := time.Date(date.Year(), date.Month(), date.Day(), hour, 0, 0, 0, time.UTC)
		consumption := GetHourlyConsumption(hour) + (rand.Float64() * 0.1)
		message := map[string]interface{}{
			"id":          "simulator-" + id,
			"consumption": consumption,
			"timestamp":   simulationTime.Format(time.RFC3339),
		}
		sendAMQPMessage(ch, "consumptionExchange", queue, message)
	}
}

func sendAMQPMessage(ch *amqp.Channel, exchange, routingKey string, data map[string]interface{}) {
	jsonData, err := json.Marshal(data)
	if err != nil {
		log.Printf("Error encoding JSON: %v", err)
		return
	}

	err = ch.Publish(
		exchange,
		routingKey,
		false,
		false,
		amqp.Publishing{
			ContentType: "application/json",
			Body:        jsonData,
		},
	)
	if err != nil {
		log.Printf("Failed to publish message to %s: %v", routingKey, err)
	} else {
		if strings.HasPrefix(routingKey, "consumption") {
			lastSuccessfulMessageTime = data["timestamp"].(string)
		}
		log.Printf("Message sent to %s via exchange %s", routingKey, exchange)
	}
}
