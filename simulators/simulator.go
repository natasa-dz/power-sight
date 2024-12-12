package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"math"
	"math/rand"
	"os"
	"strings"
	"sync"
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
var simulatorId string
var municipality string
var ch *amqp.Channel
var conn *amqp.Connection
var chMutex sync.Mutex

func main() {
	id := flag.String("id", "simulator", "Unique ID for the simulator instance")
	municipalityInput := flag.String("municipality", "novisad", "Municipality name")
	flag.Parse()

	municipality = strings.ToLower(strings.ReplaceAll(*municipalityInput, " ", ""))
	simulatorId = *id

	initializeAMQP()

	go sendHeartbeat()
	go sendConsumptionData()

	select {}
}

func initializeAMQP() {
	chMutex.Lock()
	defer chMutex.Unlock()

	// Close existing connection and channel if any
	if ch != nil {
		_ = ch.Close()
	}
	if conn != nil {
		_ = conn.Close()
	}

	// Establish new connection
	var err error
	conn, err = amqp.Dial(amqpURL)
	if err != nil {
		fmt.Printf("Failed to connect to RabbitMQ: %v", err)
	}

	// Establish new channel
	ch, err = conn.Channel()
	if err != nil {
		fmt.Printf("Failed to open a channel: %v", err)
	}

	// Declare exchanges
	err = ch.ExchangeDeclare("heartbeatExchange", "direct", true, false, false, false, nil)
	if err != nil {
		fmt.Printf("Failed to declare heartbeat exchange: %v", err)
	}
	err = ch.ExchangeDeclare("consumptionExchange", "direct", true, false, false, false, nil)
	if err != nil {
		fmt.Printf("Failed to declare consumption exchange: %v", err)
	}
}

func getChannel() *amqp.Channel {
	chMutex.Lock()
	defer chMutex.Unlock()
	return ch
}

func sendHeartbeat() {
	ticker := time.NewTicker(heartbeatInterval)
	defer ticker.Stop()

	for range ticker.C {
		message := map[string]interface{}{
			"status":    "online",
			"id":        "simulator-" + simulatorId,
			"timestamp": time.Now().Format(time.RFC3339),
		}
		sendAMQPMessage("heartbeatExchange", "heartbeat_queue_"+municipality, message, false)
	}
}

func sendConsumptionData() {
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
			"id":          "simulator-" + simulatorId,
			"consumption": consumption,
			"timestamp":   simulationTime.Format(time.RFC3339),
		}
		sendAMQPMessage("consumptionExchange", "consumption_queue_"+municipality, message, true)
	}
}

func sendAMQPMessage(exchange, routingKey string, data map[string]interface{}, isConsumption bool) {
	ch := getChannel()

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
		if isConsumption {
			consumptionStr := fmt.Sprintf("%.2f", data["consumption"].(float64))
			timestampStr := data["timestamp"].(string)
			saveFailedData(consumptionStr, timestampStr)
		}
		func() {
			defer func() {
				if r := recover(); r != nil {
					fmt.Println("Not connected:", r)
				}
			}()

			initializeAMQP()
			fmt.Println("Successful connection.")
		}()
	} else {
		if strings.HasPrefix(routingKey, "consumption") {
			lastSuccessfulMessageTime = data["timestamp"].(string)
		}
		log.Printf("Message sent to %s via exchange %s", routingKey, exchange)
	}
}

func saveFailedData(consumption string, timestamp string) {
	filename := simulatorId + ".txt"
	file, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		if !os.IsExist(err) {
			fmt.Printf("Error creating file: %v\n", err)
		}
		return
	}
	defer file.Close()
	line := fmt.Sprintf("%s,%s\n", timestamp, consumption)
	_, err = file.WriteString(line)
	if err != nil {
		fmt.Printf("Error appending to file: %v\n", err)
	}
}
