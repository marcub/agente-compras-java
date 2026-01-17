package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.model.SearchLog;
import br.com.marcusferraz.agentecompras.model.User;
import br.com.marcusferraz.agentecompras.model.enums.SearchLogStatus;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.repository.SearchLogRepository;
import br.com.marcusferraz.agentecompras.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    private final UserRepository userRepository;

    public SearchLogService(SearchLogRepository searchLogRepository, UserRepository userRepository) {
        this.searchLogRepository = searchLogRepository;
        this.userRepository = userRepository;
    }

    @Async
    public void addSearchLog(String whatsappId, String term, Integer itemsFound, Store store, SearchLogStatus searchLogStatus) {
        User user = userRepository.findByWhatsappId(whatsappId)
                .orElseThrow(() -> new RuntimeException("User not found with whatsappId: " + whatsappId));

        SearchLog newSearchLog = new SearchLog();
        newSearchLog.setUser(user);
        newSearchLog.setSearchTerm(term);
        newSearchLog.setItemsFound(itemsFound);
        newSearchLog.setStore(store);
        newSearchLog.setSearchLogStatus(searchLogStatus);

        searchLogRepository.save(newSearchLog);
    }
}
