package com.devsuperior.dscatalog.tests.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.services.CategoryService;
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
