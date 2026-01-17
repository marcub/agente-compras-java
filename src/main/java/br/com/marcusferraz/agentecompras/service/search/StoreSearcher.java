package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;

import java.util.List;

public interface StoreSearcher {
    List<ProductDTO> search(String userTerm) throws Exception;
    Store getStore();
}
