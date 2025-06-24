package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.TrainService;
import ticket.booking.services.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {
    public static void main(String[] args) {
        System.out.println("Running Train Booking System...");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService = null;
        TrainService trainService = null;
        Train trainSelected = null;
        String source = null;
        String destination = null;

        try {
            trainService = new TrainService();
            userBookingService = new UserBookingService(trainService);
        } catch (IOException e) {
            System.err.println("Failed to initialize booking service: " + e.getMessage());
            scanner.close();
            return;
        }

        while (option != 7) {
            System.out.println("\n=== Choose Option ===");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel a Booking");
            System.out.println("7. Exit the App");

            try {
                option = scanner.nextInt();
                scanner.nextLine();

                switch (option) {
                    case 1:
                        System.out.println("Enter your name: ");
                        String nameToSignUp = scanner.nextLine();
                        System.out.println("Enter your password: ");
                        String passwordToSignUp = scanner.nextLine();

                        User userToSignUp = new User(
                                nameToSignUp,
                                passwordToSignUp,
                                UserServiceUtil.hashPassword(passwordToSignUp),
                                UUID.randomUUID().toString(),
                                new ArrayList<>()
                        );

                        if (userBookingService.signUp(userToSignUp)) {
                            System.out.println("Sign-up successful!");
                            userBookingService = new UserBookingService(trainService);
                        } else {
                            System.out.println("Failed to sign up.");
                        }
                        break;

                    case 2:
                        System.out.println("Enter your name: ");
                        String nameToLogin = scanner.nextLine();
                        System.out.println("Enter your password: ");
                        String passwordToLogin = scanner.nextLine();

                        User userToLogin = new User(
                                nameToLogin,
                                passwordToLogin,
                                null,
                                null,
                                new ArrayList<>()
                        );

                        userBookingService = new UserBookingService(userToLogin, trainService);
                        if (userBookingService.loginUser()) {
                            System.out.println("Login successful!");
                        } else {
                            System.out.println("Login failed: Invalid credentials.");
                            userBookingService = new UserBookingService(trainService);
                        }
                        break;

                    case 3:
                        if (userBookingService == null) {
                            System.out.println("Booking service not initialized.");
                            break;
                        }
                        userBookingService.fetchBooking();
                        break;

                    case 4:
                        System.out.println("Enter source station: ");
                        source = scanner.nextLine();
                        System.out.println("Enter destination station: ");
                        destination = scanner.nextLine();

                        List<Train> trains = userBookingService.getTrains(source, destination);
                        if (trains.isEmpty()) {
                            System.out.println("No trains found for the route.");
                            trainSelected = null;
                            break;
                        }
                        for (int i = 0; i < trains.size(); i++) {
                            Train train = trains.get(i);
                            System.out.println((i + 1) + ". Train ID: " + train.getTrainId());
                            for (Map.Entry<String, String> entry : train.getStationTimes().entrySet()) {
                                System.out.println("  Station " + entry.getKey() + " -> Time: " + entry.getValue());
                            }
                        }
                        System.out.println("Select a train (1-" + trains.size() + ") or 0 to cancel: ");
                        int trainIndex = scanner.nextInt();
                        scanner.nextLine();
                        if (trainIndex > 0 && trainIndex <= trains.size()) {
                            trainSelected = trains.get(trainIndex - 1);
                            System.out.println("Train " + trainSelected.getTrainId() + " selected.");
                        } else {
                            System.out.println("Invalid train selection.");
                            trainSelected = null;
                        }
                        break;

                    case 5:
                        if (trainSelected == null) {
                            System.out.println("No train selected. Please search and select a train first.");
                            break;
                        }
                        if (userBookingService == null) {
                            System.out.println("Please log in first.");
                            break;
                        }
                        System.out.println("Select a seat from below: ");
                        List<List<Integer>> seats = userBookingService.fetchSeats(trainSelected);
                        if (seats == null || seats.isEmpty()) {
                            System.out.println("No seats available for this train.");
                            break;
                        }
                        for (int i = 0; i < seats.size(); i++) {
                            System.out.print("Row " + (i + 1) + ": ");
                            for (Integer val : seats.get(i)) {
                                System.out.print(val + " ");
                            }
                            System.out.println();
                        }
                        System.out.println("Enter row (1-" + seats.size() + "): ");
                        int row = scanner.nextInt() - 1;
                        System.out.println("Enter column (1-" + (row >= 0 && row < seats.size() ? seats.get(row).size() : 0) + "): ");
                        int col = scanner.nextInt() - 1;
                        scanner.nextLine();
                        System.out.println("Booking your seat...");
                        if (userBookingService.bookSeat(trainSelected, source, destination, row, col)) {
                            System.out.println("Booked! Enjoy your journey.");
                        } else {
                            System.out.println("Can't book this seat. It may be invalid or already booked.");
                        }
                        break;

                    case 6:
                        if (userBookingService == null) {
                            System.out.println("Please log in first.");
                            break;
                        }
                        if (userBookingService.cancelBooking(null, scanner)) {
                            System.out.println("Booking cancelled successfully.");
                        } else {
                            System.out.println("Failed to cancel booking.");
                        }
                        break;

                    case 7:
                        System.out.println("Exiting the Train Booking System...");
                        break;

                    default:
                        System.out.println("Invalid option. Please choose 1-7.");
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                scanner.nextLine();
            }
        }
        scanner.close();
    }
}
