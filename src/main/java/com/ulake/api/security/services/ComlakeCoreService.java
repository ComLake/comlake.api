package com.ulake.api.security.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ComlakeCoreService {
	@Value("${app.coreBasePath}")
	private String coreBasePath;

	private RestTemplate restTemplate = new RestTemplate();

	private HttpMessageConverter<?> jacksonSupportsMoreTypes() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(MediaType.parseMediaType("text/plain;charset=utf-8"),
				MediaType.APPLICATION_OCTET_STREAM));
		return converter;
	}

	// POST /file
	public String postFile(byte[] data, Long size, String mimeType) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Content-Length", size.toString());
		headers.set("Content-Type", mimeType);

		HttpEntity<byte[]> entity = new HttpEntity<>(data, headers);

		ResponseEntity<byte[]> response = restTemplate.postForEntity(coreBasePath + "file", entity, byte[].class);

		// Get and save the response cid
		ObjectMapper mapperCreate = new ObjectMapper();
		JsonNode rootCreate = mapperCreate.readTree(response.getBody());
		String cid = rootCreate.path("cid").asText();
		return cid;
	}

	// POST /dir
	public String postFolder() throws IOException {
		// Request to core POST /dir - Create an empty directory
		ResponseEntity<String> response = restTemplate.postForEntity(coreBasePath + "/dir", null, String.class);

		// Get and save the response cid
		ObjectMapper mapperCreate = new ObjectMapper();
		JsonNode rootCreate = mapperCreate.readTree(response.getBody());
		String cid = rootCreate.path("cid").asText();
		return cid;
	}

	// GET /file/{cid}
	public String getFileData(String cid) {
		String FILE_URL = coreBasePath + "file/" + cid;
		restTemplate.getMessageConverters().add(jacksonSupportsMoreTypes());
		ResponseEntity<String> response = restTemplate.getForEntity(FILE_URL, String.class);
		return response.getBody();
	}

	// POST /cp
	public String cpToDir(String src, String dest, String path) throws JsonMappingException, JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		JSONObject dataset = new JSONObject();
		dataset.put("src", src);
		dataset.put("dest", dest);
		dataset.put("path", path);

		HttpEntity<String> requestCp = new HttpEntity<String>(dataset.toString(), headers);
		ResponseEntity<String> responseCp = restTemplate.postForEntity(coreBasePath + "cp", requestCp, String.class);

		ObjectMapper mapperCp = new ObjectMapper();
		JsonNode rootCp = mapperCp.readTree(responseCp.getBody());
		String cid = rootCp.path("cid").asText();
		return cid;
	}

	// GET /dir/{cid}
	public JsonNode listContent(String cid) throws JsonMappingException, JsonProcessingException {
		ResponseEntity<String> response = restTemplate.getForEntity(coreBasePath + "dir/" + cid, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(response.getBody());

		return root;
	}

	// POST /dataset
	public String addDataset(String cid, String name, String source, List<String> topics, Long size, String mimeType,
			String language) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		JSONObject dataset = new JSONObject();
		dataset.put("file", cid);
		dataset.put("description", name);
		dataset.put("source", source);
		dataset.put("mimeType", mimeType);
		dataset.put("size", size);
		dataset.put("topics", new JSONArray(topics));
		dataset.put("language", language);

		HttpEntity<String> requestDataset = new HttpEntity<String>(dataset.toString(), headers);
		ResponseEntity<String> responseDataset = restTemplate.postForEntity(coreBasePath + "dataset", requestDataset,
				String.class);

		// Get and save the response datasetId
		ObjectMapper mapperDataset = new ObjectMapper();
		JsonNode rootDataset = mapperDataset.readTree(responseDataset.getBody());
		String datasetId = rootDataset.path("id").asText();

		return datasetId;
	}

	// POST /update
	public String updateDataset(String parent, String description, String source, List<String> topics, String language)
			throws JsonMappingException, JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		
		JSONObject dataset = new JSONObject();
		dataset.put("parent", parent);
		dataset.put("description", description);
		dataset.put("source", source);
		dataset.put("topics", new JSONArray(topics));	
		dataset.put("language", language);		

		HttpEntity<String> requestDataset = new HttpEntity<String>(dataset.toString(), headers);
		ResponseEntity<String> responseDataset = restTemplate.postForEntity(coreBasePath + "update", requestDataset,
				String.class);

		ObjectMapper mapperDataset = new ObjectMapper();
		JsonNode rootDataset = mapperDataset.readTree(responseDataset.getBody());
		String datasetId = rootDataset.path("id").asText();

		return datasetId;
	}

	// POST /find by datasetId
	public Object[] findByDatasetId(String datasetId) {
		String astQuery = "[\"==\", [\".\", [\"$\"], \"id\"], " + datasetId + "]";
		HttpEntity<String> request = new HttpEntity<String>(astQuery);
		ResponseEntity<Object[]> response = restTemplate.postForEntity(coreBasePath + "find", request, Object[].class);
		return response.getBody();
	}

	// POST /find by topics
	public Object[] findByTopics(List<String> topics) {
		String astQuery = "[\"&&\", [\".\", [\"$\"], \"topics\"], "
				+ "[" + topics.stream().collect(Collectors.joining("\", \"", "\"", "\"")) + "]" + "]";
		HttpEntity<String> request = new HttpEntity<String>(astQuery);
		ResponseEntity<Object[]> response = restTemplate.postForEntity(coreBasePath + "find", request, Object[].class);
		System.out.print(response.getBody());
		return response.getBody();
	}
}