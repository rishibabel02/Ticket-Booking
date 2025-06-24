package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {
    private User user;
    private List<User> userList = new ArrayList<>();
    private final TrainService trainService; // Injected dependency
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(TrainService trainService) throws IOException {
        this.trainService = trainService;
        loadUser();
    }

    public UserBookingService(User user, TrainService trainService) throws IOException {
        this.user = user;
        this.trainService = trainService;
        loadUser();
    }

    public List<User> loadUser() throws IOException {
        File usersFile = new File(USERS_PATH);
        if (usersFile.exists() && usersFile.length() > 0) {
            userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
        } else {
            userList = new ArrayList<>();
        }
        return userList;
    }

    public Boolean loginUser() {
        if (user == null || user.getName() == null || user.getPassword() == null) {
            System.out.println("Invalid login attempt: username or password is null.");
            return false;
        }
        Optional<User> foundUser = userList.stream()
                .filter(user1 -> user1.getName().equalsIgnoreCase(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();
        if (foundUser.isPresent()) {
            this.user = foundUser.get();
            return true;
        }
        return false;
    }

    public Boolean signUp(User user1) {
        try {
            if (userList.stream().anyMatch(u -> u.getName().equalsIgnoreCase(user1.getName()))) {
                System.out.println("Username already exists.");
                return false;
            }
            userList.add(user1);
            saveUserListToFile();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving user list: " + e.getMessage());
            return false;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBooking() {
        if (user == null || user.getName() == null || user.getPassword() == null) {
            System.out.println("Please log in first.");
            return;
        }
        Optional<User> userFetched = userList.stream()
                .filter(user1 -> user1.getName().equalsIgnoreCase(user.getName())
                        && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword()))
                .findFirst();
        if (userFetched.isPresent()) {
            List<Ticket> tickets = userFetched.get().getTicketBooked();
            if (tickets.isEmpty()) {
                System.out.println("No bookings found for this user.");
            } else {
                userFetched.get().printTickets();
            }
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    public Boolean cancelBooking(String ticketId, Scanner scanner) {
        System.out.println("Enter the ticketId to cancel: ");
        String inputTicketId = scanner.nextLine();
        if (inputTicketId == null || inputTicketId.isEmpty()) {
            System.out.println("TicketId can't be null or empty.");
            return false;
        }
        boolean removed = user.getTicketBooked().removeIf(ticket -> ticket.getTicketId().equals(inputTicketId));
        if (removed) {
            try {
                saveUserListToFile();
                System.out.println("Ticket ID: " + inputTicketId + " has been cancelled");
                return true;
            } catch (IOException e) {
                System.err.println("Error saving user list: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("No ticket found with ID " + inputTicketId);
            return false;
        }
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            return trainService.searchTrain(source, destination);
        } catch (Exception e) {
            System.err.println("Error searching trains: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train != null ? train.getSeats() : new ArrayList<>();
    }

    public Boolean bookSeat(Train train, String source, String destination, int row, int seat) {
        try {
            if (train == null || train.getSeats() == null || user == null) {
                return false;
            }
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    Ticket ticket = new Ticket(
                            UUID.randomUUID().toString(),
                            source,
                            user.getUserId(),
                            destination,
                            new Date(),
                            train
                    );
                    user.getTicketBooked().add(ticket);
                    saveUserListToFile();
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            System.err.println("Error booking seat: " + e.getMessage());
            return false;
        }
    }
}
