package tn.transporttn.dto.commun;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Page de résultats, au format simple attendu par le frontend. */
public record PageResponse<T>(
        List<T> contenu,
        int page,
        int taille,
        long totalElements,
        int totalPages,
        boolean derniere
) {
    public static <E, T> PageResponse<T> depuis(Page<E> page, Function<E, T> conversion) {
        return new PageResponse<>(
                page.getContent().stream().map(conversion).toList(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}