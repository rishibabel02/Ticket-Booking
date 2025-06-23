package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserBookingService {
    private User user;
    private List<User> userList;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        File users = new File(USERS_PATH);

//        Here I am deserializing users.json to be used in the future.
//        json --> Object(user) ---> deserialize
//        Object(user) --> json ---> serialize
        userList= objectMapper.readValue(users, new TypeReference<List<User>> () {});
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



    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;

        }catch (IOException e){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        objectMapper.writeValue(usersFile, userList);
    }
}
