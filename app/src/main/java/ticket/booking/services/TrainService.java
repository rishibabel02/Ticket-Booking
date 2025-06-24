package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {
    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_DB_PATH = "app/src/main/java/ticket/booking/localDb/trains.json";

    public List<Train> loadTrain() throws IOException {
        File trains = new File(TRAIN_DB_PATH);
        return objectMapper.readValue(trains, new TypeReference<List<Train>>() {});
    }

    public TrainService() throws IOException{
        loadTrain();
    }

    public List<Train> searchTrain(String source, String destination){
        return trainList.stream().filter(train -> validTrain(train, source, destination)).collect(Collectors.toList());
    }

    private Boolean validTrain(Train train, String source, String destination){
        List<String> stationOrder = train.getStations();

        int sourceIndex = stationOrder.indexOf(source.toLowerCase());
        int destIndex = stationOrder.indexOf(destination.toLowerCase());

        return sourceIndex != -1 && destIndex != -1 && sourceIndex <destIndex;
    }

    public void addTrain(Train newTrain){
        Optional<Train> exist = trainList.stream()
                .filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId()))
                .findFirst();

        if(exist.isPresent()){
            updateTrain(newTrain);
        }
        else{
                trainList.add(newTrain);
                saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain){
        OptionalInt index = IntStream.range(0, trainList.size())
                .filter(i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();

        if(index.isPresent()){
            trainList.set(index.getAsInt(), updatedTrain);
            saveTrainListToFile();
        }else {

            addTrain(updatedTrain);
        }
    }

    public void saveTrainListToFile(){
        try{
            objectMapper.writeValue(new File(TRAIN_DB_PATH), trainList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
