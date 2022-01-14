package com.devsuperior.dscatalog.tests.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.factory.ProductFactory;


@SpringBootTest
@AutoConfigureMockMvc										// não carrega o Tomcat
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService productService; 

	@Value("${security.oauth2.client.client-id}")
	private String clientId;
	
	@Value("${security.oauth2.client.client-secret}")
	private String clientSecret;
	
	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private ProductDTO newProductDTO;
	private ProductDTO existingProductDTO;
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setup() throws Exception {
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		newProductDTO = ProductFactory.createProductDTO(null);
		existingProductDTO = ProductFactory.createProductDTO(existingId);
		page = new PageImpl<>(List.of(existingProductDTO));
	}	
	
	@Test
	public void findByIdShouldReturnProduct_whenIdExists() throws Exception {
		when(productService.findById(existingId)).thenReturn(existingProductDTO);
		
		ResultActions result = mockMvc.perform(get("/products/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(existingProductDTO.getName()));
		result.andExpect(jsonPath("$.price").value(existingProductDTO.getPrice()));
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundException_whenIdDoesNotExist() throws Exception {
		when(productService.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		
		ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isNotFound());		
	}
	
	@Test
	public void findAllShouldReturnPage() throws Exception {
		when(productService.findAllPaged(any(), anyString(), any())).thenReturn(page);
						
		ResultActions result = mockMvc.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content").exists());
	}
	
	@Test
	public void insertShouldReturnProductDTO() throws Exception {
		when(productService.insert(any())).thenReturn(existingProductDTO);
		
	}

	@Test
	public void updateShouldReturnProductDTO() throws Exception {
		when(productService.update(eq(existingId), any())).thenReturn(existingProductDTO);				// eq = permite passar um valor, qndo o outro parametro é any()
							
	}	
	
	@Test
	public void updateShouldThrowResourceNotFoundException_whenIdDoesNotExist() throws Exception {
		when(productService.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);				
						
	}	
	
	@Test
	public void deleteShouldDoNothing_whenIdExist() throws Exception {
		doNothing().when(productService).delete(existingId);				
						
	}	
	
	@Test
	public void deleteShouldThrowResourceNotFoundException_whenIdDoesNotExist() throws Exception {
		doThrow(ResourceNotFoundException.class).when(productService).delete(nonExistingId);				
						
	}
	
	@Test
	public void deleteShouldThrowDatabaseException_whenIdIsDependent() throws Exception {
		doThrow(DatabaseException.class).when(productService).delete(dependentId);				
						
	}	
	
	private String obtainAccessToken(String username, String password) throws Exception {
		 
	    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	    params.add("grant_type", "password");
	    params.add("client_id", clientId);
	    params.add("username", username);
	    params.add("password", password);
	 
	    ResultActions result 
	    	= mockMvc.perform(post("/oauth/token")
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