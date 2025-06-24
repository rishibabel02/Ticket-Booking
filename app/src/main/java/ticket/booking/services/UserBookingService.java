package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {
    private User user;
    private List<User> userList;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";


    public UserBookingService(User user1) throws IOException {
        this.user = user1;

//        File users = new File(USERS_PATH);
//        Here I am deserializing users.json to be used in the future.
//        json --> Object(user) ---> deserialize
//        Object(user) --> json ---> serialize
//        userList= objectMapper.readValue(users, new TypeReference<List<User>> () {});

        loadUser();
    }


    public UserBookingService() throws IOException {
        loadUser();
    }

    public List<User> loadUser() throws IOException {
        File users = new File(USERS_PATH);
        return objectMapper.readValue(users, new TypeReference<List<User>>() {
        });
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream()
                .filter(user1 -> {
                    return user1.getName().equalsIgnoreCase(user.getName())
                            && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
                })
                .findFirst();

        return foundUser.isPresent();
    }


    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;

        } catch (IOException e) {
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBooking() {
        Optional<User> userFetched = userList.stream()
                .filter(user1 -> {
                    return user1.getName().equalsIgnoreCase(user.getName())
                            && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
                })
                .findFirst();

        if (userFetched.isPresent()) {
            userFetched.get().printTickets();
        }
    }

    public Boolean cancelBooking(String ticketId) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter the ticketId to cancel:  ");
        ticketId = s.next();

        if (ticketId == null || ticketId.isEmpty()) {
            System.out.println("TicketId can't be null or empty.");
            return Boolean.FALSE;
        }

        String ticketToBeRemoved = ticketId;
        boolean removed = user.getTicketBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketToBeRemoved));

        if (removed) {
            System.out.println("Ticket ID: " + ticketId + " has been cancelled");
            return Boolean.TRUE;
        } else {
            System.out.println("No ticket found with ID " + ticketId);
            return Boolean.FALSE;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrain(source, destination);

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookSeat(Train train, int row, int seat){
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            if(row >=0 && row < seats.size() && seat > 0 && seat < seats.get(row).size());

        } catch (Exception e) {

        }
    }

}
