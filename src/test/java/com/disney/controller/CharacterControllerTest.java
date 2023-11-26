package com.disney.controller;

import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.ApiErrorResponse;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;
import com.disney.service.CharacterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.disney.model.HttpCodeResponse.*;
import static com.disney.util.ApiUtils.*;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CharacterController.class)
public class CharacterControllerTest {
    private final String URL_TEMPLATE = CHARACTER_BASE_URL + CHARACTER_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final CharacterService characterService;

    private CharacterRequestDto createCharacterRequest;
    private CharacterUpdateRequestDto updateCharacterRequest;

    @Autowired
    public CharacterControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, CharacterService characterService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.characterService = characterService;
    }

    @BeforeEach
    void setUp() {
        createCharacterRequest = new CharacterRequestDto(
                "character-image.jpg",
                "Character Name",
                29,
                89.9,
                "Character History",
                emptySet()
        );
        updateCharacterRequest = new CharacterUpdateRequestDto(
                "new-image", "New Name", 100, 80.5, "New history", emptySet(), emptySet()
        );
    }

    @DisplayName(value = "JUnit Test for successfully create a Character")
    @Test
    public void givenRequest_whenTryToCreateCharacter_thenStatusIsCreated() throws Exception {
        // given
        willDoNothing().given(characterService).createCharacter(any(CharacterRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCharacterRequest)));

        //then
        then(characterService).should(times(1)).createCharacter(createCharacterRequest);
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for create Character null or empty required values")
    @Test
    public void givenRequestWithoutRequiredValues_whenTryToCreateCharacter_thenStatusIsBadRequest() throws Exception {
        final String msgEmptyName = "The name can't be empty or null";
        final String msgBlankName = "The name can't be whitespaces";
        final String msgNullAge = "The age can't be null";
        final String msgMinAge = "Positive numbers only, minimum is 1";
        final String msgNullWeight = "The weight can't be null";
        final String msgMinWeight = "Positive numbers only, minimum is 1";
        final String msgEmptyHistory = "The history can't be empty or null";
        final String msgBlankHistory = "The history can't be whitespaces";

        // given
        @SuppressWarnings("DataFlowIssue") final CharacterRequestDto invalidRequest =
                new CharacterRequestDto(null, null, 0, 0.0, null, emptySet());

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        //then
        then(characterService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(is(msgEmptyName), is(msgBlankName))))
                .andExpect(jsonPath("$.age", anyOf(is(msgNullAge), is(msgMinAge))))
                .andExpect(jsonPath("$.weight", anyOf(is(msgNullWeight), is(msgMinWeight))))
                .andExpect(jsonPath("$.history", anyOf(is(msgEmptyHistory), is(msgBlankHistory))));
    }

    @DisplayName(value = "JUnit Test for try to create Character without required body")
    @Test
    public void givenRequestWithoutBody_whenTryToCreateCharacter_thenStatusIsBadRequest() throws Exception {
        // given
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path("uri=" + CHARACTER_BASE_URL)
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON));

        //then
        then(characterService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for create Character but the Character is already registered")
    @Test
    public void givenRequest_whenTryToCreateDuplicatedCharacter_thenStatusIsBadRequest() throws Exception {
        // given
        final String errorMsg = "The character '%s' is already registered".formatted(createCharacterRequest.name());
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(DUPLICATED_RESOURCE)
                .path("uri=" + CHARACTER_BASE_URL)
                .message(errorMsg)
                .build();
        willThrow(new EntityExistsException(errorMsg)).given(characterService)
                .createCharacter(any(CharacterRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCharacterRequest)));

        //then
        then(characterService).should(times(1)).createCharacter(createCharacterRequest);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get all Characters (no specification)")
    @Test
    public void givenRequest_whenTryToListCharacters_thenReturnListWithAllCharacters() throws Exception {
        // given
        final int pageNumber = 0;
        final List<CharacterResponseDto> mockList = List
                .of(mock(CharacterResponseDto.class), mock(CharacterResponseDto.class));
        final PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE);
        final Page<CharacterResponseDto> responseList = new PageImpl<>(mockList, pageable, mockList.size());

        given(characterService.listCharacters(pageNumber, null, 0, null)).willReturn(responseList);

        // when
        ResultActions response = mockMvc.perform(get(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", String.valueOf(pageNumber)));

        //then
        then(characterService).should(times(1)).listCharacters(pageNumber, null, 0, null);
        response.andDo(print())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @DisplayName(value = "JUnit Test for get all Characters that match with a specific name")
    @Test
    public void givenNameSpecification_whenListCharacters_thenReturnListWithAllCharacters() throws Exception {
        // given
        final int pageNumber = 0;
        final String characterName = "Character Name";
        final List<CharacterResponseDto> characterResponseDtoList = List.of(
                new CharacterResponseDto(
                        UUID.randomUUID().toString(),
                        "character-image.jpg",
                        characterName,
                        31,
                        90.5,
                        "Some history context",
                        emptySet()),
                new CharacterResponseDto(
                        UUID.randomUUID().toString(),
                        "random-image.png",
                        characterName,
                        26,
                        80.4,
                        "History example",
                        Set.of(mock(MovieBasicInfoResponseDto.class)))
        );
        final PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE);
        final Page<CharacterResponseDto> responseList = new PageImpl<>(
                characterResponseDtoList, pageable, characterResponseDtoList.size()
        );

        given(characterService.listCharacters(pageNumber, characterName, 0, null)).willReturn(responseList);

        // when
        ResultActions response = mockMvc.perform(get(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", String.valueOf(pageNumber))
                .param("name", characterName));

        //then
        then(characterService).should(times(1)).listCharacters(pageNumber, characterName, 0, null);
        response.andDo(print())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @DisplayName(value = "JUnit Test for successfully update a Character")
    @Test
    public void givenUpdateRequestAndId_whenTryToUpdateCharacter_thenReturnCharacterUpdated() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final CharacterResponseDto expectedResponse = new CharacterResponseDto(
                characterId,
                updateCharacterRequest.image(),
                updateCharacterRequest.name(),
                updateCharacterRequest.age(),
                updateCharacterRequest.weight(),
                updateCharacterRequest.history(),
                emptySet()
        );

        given(characterService.updateCharacter(anyString(), any(CharacterUpdateRequestDto.class)))
                .willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCharacterRequest)));

        //then
        then(characterService).should(times(1)).updateCharacter(characterId, updateCharacterRequest);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.image", is(expectedResponse.image())))
                .andExpect(jsonPath("$.name", is(expectedResponse.name())))
                .andExpect(jsonPath("$.age", is(expectedResponse.age())))
                .andExpect(jsonPath("$.weight", is(expectedResponse.weight())))
                .andExpect(jsonPath("$.history", is(expectedResponse.history())))
                .andExpect(jsonPath("$.movies", empty()));
    }

    @DisplayName(value = "JUnit Test for update Character and the Character to update is not present in the data base")
    @Test
    public void givenRequestAndId_whenTryToUpdateAndCharacterIsNotPresent_thenStatusIsNotFound() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final String errorMsg = "Character not found for ID %s".formatted(characterId);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path("uri=" + CHARACTER_BASE_URL + '/' + characterId)
                .message(errorMsg)
                .build();
        given(characterService.updateCharacter(anyString(), any(CharacterUpdateRequestDto.class)))
                .willThrow(new EntityNotFoundException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCharacterRequest)));

        //then
        then(characterService).should(times(1)).updateCharacter(characterId, updateCharacterRequest);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for update Character with invalid ID format as path variable")
    @Test
    public void givenInvalidIdFormat_whenUpdateCharacter_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "null";
        final String errorMsg = "Invalid UUID string: %s".formatted(invalidIdFormat);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path("uri=" + CHARACTER_BASE_URL + '/' + invalidIdFormat)
                .message(errorMsg)
                .build();
        given(characterService.updateCharacter(anyString(), any(CharacterUpdateRequestDto.class)))
                .willThrow(new InvalidUUIDFormatException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCharacterRequest)));

        //then
        then(characterService).should(times(1)).updateCharacter(invalidIdFormat, updateCharacterRequest);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for bad request when update Character without required body")
    @Test
    public void givenMissingRequestBody_whenTryToUpdateCharacter_thenStatusIsBadRequest() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path("uri=" + CHARACTER_BASE_URL + '/' + characterId)
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON));

        //then
        then(characterService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for successfully delete Character by ID")
    @Test
    public void givenCharacterId_whenDeleteCharacter_thenCharacterIsDeletedAndStatusIsNoContent() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        willDoNothing().given(characterService).deleteCharacter(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).deleteCharacter(characterId);
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @DisplayName(value = "JUnit Test for delete Character with an invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenTryToDeleteCharacter_thenThrowsAndStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "null";
        final String errorMsg = "Invalid UUID string: %s".formatted(invalidIdFormat);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path("uri=" + CHARACTER_BASE_URL + '/' + invalidIdFormat)
                .message(errorMsg)
                .build();
        willThrow(new InvalidUUIDFormatException(errorMsg)).given(characterService).deleteCharacter(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).deleteCharacter(invalidIdFormat);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for delete Character but is not present in the database")
    @Test
    public void givenCharacterId_whenTryToDeleteCharacter_thenThrowForNotPresentAndStatusIsNotFound() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final String errorMsg = "Character not found for ID %s".formatted(characterId);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path("uri=" + CHARACTER_BASE_URL + '/' + characterId)
                .message(errorMsg)
                .build();
        willThrow(new EntityNotFoundException(errorMsg)).given(characterService).deleteCharacter(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).deleteCharacter(characterId);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get Character by ID")
    @Test
    public void givenId_whenGetCharacterById_thenReturnTheCharacterAndStatusIsOk() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final CharacterResponseDto expectedResponse = new CharacterResponseDto(
                characterId,
                "character-image.jpg",
                "Character Name",
                31,
                92.5,
                "Character history",
                emptySet()
        );
        given(characterService.getCharacterById(anyString())).willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).getCharacterById(characterId);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.image", is(expectedResponse.image())))
                .andExpect(jsonPath("$.name", is(expectedResponse.name())))
                .andExpect(jsonPath("$.age", is(expectedResponse.age())))
                .andExpect(jsonPath("$.weight", is(expectedResponse.weight())))
                .andExpect(jsonPath("$.history", is(expectedResponse.history())))
                .andExpect(jsonPath("$.movies", empty()));
    }

    @DisplayName(value = "JUnit Test for get Character with invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenTryToGetCharacterById_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "null";
        final String errorMsg = "Invalid UUID string: %s".formatted(invalidIdFormat);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path("uri=" + CHARACTER_BASE_URL + '/' + invalidIdFormat)
                .message(errorMsg)
                .build();
        given(characterService.getCharacterById(anyString())).willThrow(new InvalidUUIDFormatException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).getCharacterById(invalidIdFormat);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get Character by ID and the Character is not present in the database")
    @Test
    public void givenIn_whenTryToGetCharacterById_thenStatusIsNotFound() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final String errorMsg = "Character not found for ID %s".formatted(characterId);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path("uri=" + CHARACTER_BASE_URL + '/' + characterId)
                .message(errorMsg)
                .build();
        given(characterService.getCharacterById(anyString())).willThrow(new EntityNotFoundException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON));

        //then
        then(characterService).should(times(1)).getCharacterById(characterId);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }
}
