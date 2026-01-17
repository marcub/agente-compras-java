package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.dto.UserDTO;
import br.com.marcusferraz.agentecompras.model.User;
import br.com.marcusferraz.agentecompras.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUserIfNotExists(UserDTO userDTO) {
        return userRepository.findByWhatsappId(userDTO.whatsappId())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setWhatsappId(userDTO.whatsappId());
                    newUser.setName(userDTO.name());
                    return userRepository.save(newUser);
                });
    }

}
