package br.com.taina.copy_twitter.dto;

import java.util.List;

// Esse vai ser o feed com paginação que recebe um item, uma postagem
public record FeedDto(List<FeedItemDto> feedItens,
                      int page,
                      int pageSize,
                      int totalPages,
                      long totalElements) {
}
