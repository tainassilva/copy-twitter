package br.com.taina.copy_twitter.controller;

import br.com.taina.copy_twitter.dto.CreateTweetDto;
import br.com.taina.copy_twitter.dto.FeedDto;
import br.com.taina.copy_twitter.dto.FeedItemDto;
import br.com.taina.copy_twitter.entity.Role;
import br.com.taina.copy_twitter.entity.Tweet;
import br.com.taina.copy_twitter.repository.TweetRepository;
import br.com.taina.copy_twitter.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class TweetController {

    private final TweetRepository tweetRepository;

    private final UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        // Busca todos os tweets do banco de dados, paginados e ordenados pelo timestamp de criação em ordem decrescente
        var tweets = tweetRepository.findAll(
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))

                // Converte cada tweet encontrado em um objeto FeedItemDto (DTO usado para exibir os tweets no feed)
                .map(tweet -> new FeedItemDto(
                        tweet.getTweetId(),
                        tweet.getContent(),
                        tweet.getUser().getUsername() // Nome do usuário que postou o tweet
                ));

        return ResponseEntity.ok(new FeedDto(
                tweets.getContent(), // Lista dos tweets formatados como FeedItemDto
                page, // Página atual
                pageSize, // Tamanho da página
                tweets.getTotalPages(), // Total de páginas disponíveis
                tweets.getTotalElements() // Total de tweets no banco
        ));
    }


    @PostMapping("/tweets")
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto createTweetDto,
                                            JwtAuthenticationToken token) { // Recebe o DTO do tweet e o token JWT do usuário autenticado

        // Busca o usuário no banco de dados pelo ID extraído do token JWT
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = new Tweet();
        // Associa o tweet ao usuário autenticado (o usuário está vindo do banco)
        tweet.setUser(user.get());
        tweet.setContent(createTweetDto.content());

        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/tweets/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId,
                                            JwtAuthenticationToken token){

        var user = userRepository.findById(UUID.fromString(token.getName()));

        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if(isAdmin || tweet.getUser().getUserId().equals(UUID.fromString(token.getName()))){
            tweetRepository.deleteById(tweetId);
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().build();
    }

}
