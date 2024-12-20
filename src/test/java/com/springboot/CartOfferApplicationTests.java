package com.springboot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {

	//It contains 10 test cases out of which 4 are valid failures/bugs
	// discount value cant be greater than cart value
	// final cart value can't be negative
	// you should be able to update the restaurant offer for a segment from flatx to flatx% or vice versa
	// invalid offer type should not be updated like flat200

	//check e2e test case where p1 r1 has flatx offer
	//p2 r1 has flatx% offer and p3r1 with no offer
	@Test
	public void checkAllOfferTypesInOneRestaurant() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		OfferRequest offerRequest = new OfferRequest(4,"FLATX",10, Arrays.asList("p1"));
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).isEqualTo(true);
		OfferRequest offerRequestPercentage = new OfferRequest(4,"FLATX%",10, Arrays.asList("p2"));
		boolean resultForOfferPercentage = addOffer(offerRequestPercentage);
		softAssertions.assertThat(resultForOfferPercentage).isEqualTo(true);
		ApplyOfferRequest applyOfferRequestTypeFlat = new ApplyOfferRequest(200,4,1);
		boolean applyofferFlat = verifyCartValue(applyOfferRequestTypeFlat,190 );
		softAssertions.assertThat(applyofferFlat).as("cart value after apply offer").isEqualTo(true);
		ApplyOfferRequest applyOfferRequestTypePercentage = new ApplyOfferRequest(200,4,2);
		boolean applyofferTypePercentage = verifyCartValue(applyOfferRequestTypePercentage,180);
		softAssertions.assertThat(applyofferTypePercentage).as("cart value after apply offer").isEqualTo(true);
		ApplyOfferRequest applyOfferRequestNoOffer = new ApplyOfferRequest(200,4,3);
		boolean applyofferTypeNoOffer = verifyCartValue(applyOfferRequestNoOffer,200);
		softAssertions.assertThat(applyofferTypeNoOffer).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();

	}

	//    check for restaurant having flat x offer for all segments
	@Test
	public void checkFlatXForAllSegments() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		OfferRequest offerRequest = new OfferRequest(5,"FLATX",10,Arrays.asList("p1","p2","p3"));
		boolean result = addOffer(offerRequest);
		ApplyOfferRequest applyOfferRequestSegment = new ApplyOfferRequest(200,5,1);
		boolean cartValueResult = verifyCartValue(applyOfferRequestSegment,190);
		softAssertions.assertThat(cartValueResult).as("cart value after apply offer").isEqualTo(true);
		applyOfferRequestSegment = new ApplyOfferRequest(200,5,2);
		result = verifyCartValue(applyOfferRequestSegment,190);
		softAssertions.assertThat(cartValueResult).as("cart value after apply offer").isEqualTo(true);
		applyOfferRequestSegment = new ApplyOfferRequest(200,5,3);
		cartValueResult = verifyCartValue(applyOfferRequestSegment,190);
		softAssertions.assertThat(cartValueResult).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();
	}

	// updating offer for a restaurant initially took flatx then flat% for cart value 200, it should return 180
	@Test
	public void checkOfferUpdateInSameSegmentForRestraunt() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		OfferRequest offerRequest = new OfferRequest(5,"FLATX",10, Arrays.asList("p1"));
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).isEqualTo(true);
		OfferRequest offerRequestPercentage = new OfferRequest(5,"FLATX%",10, Arrays.asList("p1"));
		boolean resultForOfferPercentage = addOffer(offerRequestPercentage);
		softAssertions.assertThat(resultForOfferPercentage).isEqualTo(true);
		ApplyOfferRequest applyOfferRequestTypeFlat = new ApplyOfferRequest(200,4,1);
		boolean applyofferFlat = verifyCartValue(applyOfferRequestTypeFlat,180 );
		softAssertions.assertThat(applyofferFlat).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();
	}

	@Test
	public void checkFlatXForSegmentP1() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true); // able to add offer
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		System.out.println(POST_PARAMS);
		OutputStream os = con.getOutputStream();
		System.out.println(os);
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println("response for add offer");
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}


	// checking for restaurants without any offer to check backward compatibility for existing flow
	@Test
	public void checkCartValueForRestaurantWithoutAnyOffer() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200,3,1);
		String applyOfferresult = applyOffer(applyOfferRequest);
		System.out.println(applyOfferresult);
		boolean applyoffer = false;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(applyOfferresult);
		if(jsonNode.get("cart_value").intValue()==200){
			applyoffer = true;
		}
		softAssertions.assertThat(applyoffer).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();
	}


	//negative case when discount value > cart value for offer flatx case
	@Test
	public void checkIfDiscountValueGreaterThanCartValue() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(2,"FLATX",201,segments);
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).as("offer added successfully").isEqualTo(true);
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200,2,1);
		String applyOfferresult = applyOffer(applyOfferRequest);
		System.out.println(applyOfferresult);
		boolean applyoffer = false;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(applyOfferresult);
		if(jsonNode.get("cart_value").intValue()>0){
			applyoffer = true;
		}
		softAssertions.assertThat(applyoffer).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();

	}

	// negative test case to check for boundary value cases here amount calculated based on discount percentage is > Cart value
	@Test
	public void checkIfDiscountValueforTypeFlatPercentageGreaterThanCartValue() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		List<String> segments = new ArrayList<>();
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(2,"FLATX%",101,segments);
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).as("offer added successfully").isEqualTo(true);
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200,2,2);
		String applyOfferresult = applyOffer(applyOfferRequest);
		System.out.println(applyOfferresult);
		boolean applyoffer = false;
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(applyOfferresult);
		if(jsonNode.get("cart_value").intValue()>0){
			applyoffer = true;
		}
		System.out.println(jsonNode.get("cart_value").intValue());
		System.out.println("apply offer -->"+ applyoffer);
		softAssertions.assertThat(false).as("cart value after apply offer").isEqualTo(true);
		softAssertions.assertAll();

	}

	@Test
	public void checkCartValueforFlatXForSegmentP1() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, segments);
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).as("offer added successfully").isEqualTo(true);
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, 1);

		String applyOfferresult = applyOffer(applyOfferRequest);
		boolean applyoffer = false;
		if (applyOfferresult.contains("190")) {
			applyoffer = true;
		}
		softAssertions.assertThat(applyoffer).as("cart value after apply offer").isEqualTo(true); // able to add offer
		softAssertions.assertAll();
	}


	@Test
	public void checkCartValueforFlatXPercentageForSegmentP2() throws Exception {
		SoftAssertions softAssertions = new SoftAssertions();
		List<String> segments = new ArrayList<>();
		segments.add("p2");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX%",10,segments);
		boolean result = addOffer(offerRequest);
		softAssertions.assertThat(result).as("offer added successfully").isEqualTo(true);
		ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200,1,2);

		String applyOfferresult = applyOffer(applyOfferRequest);
		boolean applyoffer = false;
		if(applyOfferresult.contains("180")){
			applyoffer = true;
		}
		softAssertions.assertThat(applyoffer).as("cart value after apply offer").isEqualTo(true); // able to add offer
		softAssertions.assertAll();
	}


	// This test case will try to apply invalid offer to a restaurant, offer api should return 400 with valid error message
	@Test
	public void applyInvalidOffer() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(3, "FLAT200", 200, segments); // Invalid offer type "FLAT10"
		boolean result = addOffer(offerRequest);
		Assert.assertFalse(result);
	}


	// This function will help in call apply offer api to figure out cart value based on passed parameters
	public String applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/cart/apply_offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(applyOfferRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println("in apply offer response");
			System.out.println(response.toString());
			return response.toString();

		} else {
			System.out.println("POST request did not work.");
		}
		return "";
	}

	// it will check for expected cart value and return boolean based on expected cart value
	public boolean verifyCartValue(ApplyOfferRequest applyOfferRequest,int cartValue) throws Exception {

		String applyOfferresult = applyOffer(applyOfferRequest);
		System.out.println(applyOfferresult);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(applyOfferresult);
		if(jsonNode.get("cart_value").intValue()==cartValue){
			return true;
		}
		return false;
	}


}
