package com.devsuperior.dscatalog.tests.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.services.CategoryService;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.CategoryFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryResourceTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private CategoryService categoryService; 

	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private CategoryDTO newCategoryDTO;
	private CategoryDTO existingCategoryDTO;
	private PageImpl<CategoryDTO> page;
	private String operatorUsername;
	private String operatorPassword;

	@BeforeEach
	void setup() throws Exception {
		operatorUsername = "alex@gmail.com";
		operatorPassword = "123456";
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		newCategoryDTO = CategoryFactory.createCategoryDTO(null);
		existingCategoryDTO = CategoryFactory.createCategoryDTO(existingId);
		page = new PageImpl<>(List.of(existingCategoryDTO));
	}
	
	@Test
	public void findById_ShouldReturnCategory_whenIdExists() throws Exception {
		when(categoryService.findById(existingId)).thenReturn(existingCategoryDTO);
		
		ResultActions result = mockMvc.perform(get("/categories/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(existingCategoryDTO.getName()));
	}
	
	@Test
	public void findById_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() throws Exception {
		when(categoryService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		ResultActions result = mockMvc.perform(get("/categories/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isNotFound());		
	}
	
	@Test
	public void findAll_ShouldReturnPage() throws Exception {
		when(categoryService.findAllPaged(any())).thenReturn(page);
						
		ResultActions result = mockMvc.perform(get("/categories")
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content").exists());
	}
	
	@Test
	public void insert_ShouldReturnCategoryDTOCreated_whenValidData() throws Exception {
		when(categoryService.insert(any())).thenReturn(newCategoryDTO);
		
		String accessToken = obtainAccessToken(operatorUsername, operatorPassword);
		String jsonBody = mapper.writeValueAsString(newCategoryDTO);

		ResultActions result = mockMvc.perform(post("/categories")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").value(newCategoryDTO.getId()));
		result.andExpect(jsonPath("$.name").value(newCategoryDTO.getName()));
		verify(categoryService, times(1)).insert(any());
	}
	
	@Test
	public void update_ShouldReturnCategoryDTO_whenIdExists() throws Exception {
		when(categoryService.update(eq(existingId), any())).thenReturn(existingCategoryDTO);				// eq = permite passar um valor, qndo o outro parametro é any()
		
		String accessToken = obtainAccessToken(operatorUsername, operatorPassword);		
		String jsonBody = mapper.writeValueAsString(existingCategoryDTO);
		
		ResultActions result = mockMvc.perform(put("/categories/{id}", existingId)
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").value(existingCategoryDTO.getId()));
		result.andExpect(jsonPath("$.name").value(existingCategoryDTO.getName()));
	}	
	
	@Test
	public void update_ShouldThrowResourceNotFoundException_whenIdDoesNotExist() throws Exception {
		when(categoryService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);	

		String accessToken = obtainAccessToken(operatorUsername, operatorPassword);		
		String jsonBody = mapper.writeValueAsString(existingCategoryDTO);		
		
		ResultActions result = mockMvc.perform(put("/categories/{id}", nonExistingId)
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isNotFound());						
	}	
		
	private String obtainAccessToken(String username, String password) throws Exception {		 
	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("grant_type", "password");
	    params.add("client_id", clientId);
	    params.add("username", username);
	    params.add("password", password);
	 
	    ResultActions result = mockMvc.perform(post("/oauth/token")
	    		.params(params)
	    		.with(httpBasic(clientId, clientSecret))
	    		.accept("application/json;charset=UTF-8"))
	        	.andExpect(status().isOk())
	        	.andExpect(content().contentType("application/json;charset=UTF-8"));
	 
	    String resultString = result.andReturn().getResponse().getContentAsString();
	 
	    JacksonJsonParser jsonParser = new JacksonJsonParser();
	    return jsonParser.parseMap(resultString).get("access_token").toString();
	}	
}
