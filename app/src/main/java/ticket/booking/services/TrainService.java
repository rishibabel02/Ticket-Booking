package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrainService {
    private List<Train> trainList = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "app/src/main/java/ticket/booking/localDb/trains.json";

    public TrainService() throws IOException {
        loadTrain();
    }

    public List<Train> loadTrain() throws IOException {
        File trainsFile = new File(TRAIN_DB_PATH);
        if (trainsFile.exists() && trainsFile.length() > 0) {
            trainList = objectMapper.readValue(trainsFile, new TypeReference<List<Train>>() {});
        } else {
            trainList = new ArrayList<>();
        }
        return trainList;
    }

    public List<Train> searchTrain(String source, String destination) {
        return trainList.stream()
                .filter(train -> validTrain(train, source, destination))
                .collect(Collectors.toList());
    }

    private Boolean validTrain(Train train, String source, String destination) {
        List<String> stationOrder = train.getStations();
        if (stationOrder == null) {
            return false;
        }
        boolean sourceExists = stationOrder.stream().anyMatch(s -> s.equalsIgnoreCase(source));
        boolean destExists = stationOrder.stream().anyMatch(s -> s.equalsIgnoreCase(destination));
        return sourceExists && destExists;
    }

    public void addTrain(Train newTrain) {
        try {
            for (int i = 0; i < trainList.size(); i++) {
                if (trainList.get(i).getTrainId().equalsIgnoreCase(newTrain.getTrainId())) {
                    trainList.set(i, newTrain);
                    saveTrainListToFile();
                    return;
                }
            }
            trainList.add(newTrain);
            saveTrainListToFile();
        } catch (Exception e) {
            System.err.println("Error adding train: " + e.getMessage());
        }
    }

    public void updateTrain(Train updatedTrain) {
        addTrain(updatedTrain); // Reuse addTrain logic
    }

    public void saveTrainListToFile() {
        try {
            File trainsFile = new File(TRAIN_DB_PATH);
            objectMapper.writeValue(trainsFile, trainList);
        } catch (IOException e) {
            System.err.println("Error saving train list: " + e.getMessage());
        }
    }
}
