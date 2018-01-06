/**
 * 
 */
package com.synectiks.services.migration.utils;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.synectiks.commons.constants.IConsts;
import com.synectiks.commons.utils.IUtils;

/**
 * @author Rajesh
 *
 */
public interface IStateUtils {

	/**
	 * Method to fetch state machine
	 * @param rest
	 * @param url
	 * @param machineId
	 * @return
	 */
	static String getMachineId(RestTemplate rest, String url, String machineId) {
		Map<String, Object> params = IUtils.getRestParamMap(IConsts.PRM_MACHINE_ID,
				machineId);
		return IUtils.sendGetRestRequest(rest, url + IConsts.API_MACHINE_ID, params,
				String.class);
	}

	/**
	 * Method to fetch state machine current name
	 * @param rest
	 * @param url
	 * @param machineId
	 * @return
	 */
	static String getCurrentState(RestTemplate rest, String url, String machineId) {
		Map<String, Object> params = IUtils.getRestParamMap(IConsts.PRM_MACHINE_ID,
				machineId);
		return IUtils.sendPostRestRequest(rest, url + IConsts.API_MACHINE_STATE, null,
				String.class, params, MediaType.APPLICATION_FORM_URLENCODED);
	}

	/**
	 * Method to send sendEvent call in state machine
	 * @param rest
	 * @param url
	 * @param ssmId
	 * @param event
	 * @return
	 */
	static boolean sendEvent(RestTemplate rest, String url, String ssmId, String event) {
		MediaType type = MediaType.APPLICATION_FORM_URLENCODED;
		Map<String, Object> params = IUtils.getRestParamMap(IConsts.PRM_EVENT, event,
					IConsts.PRM_MACHINE_ID, ssmId);
		IUtils.logger.info("Params: " + params);
		return IUtils.sendPostRestRequest(rest, url + IConsts.API_MACHINE_EVENT, null,
				Boolean.class, params, type);
	}

	/**
	 * Method send request to add extended variable into state machine
	 * @param rest
	 * @param url
	 * @param ssmId
	 * @param key
	 * @param value
	 * @return 
	 */
	static boolean addExtendedVariable(RestTemplate rest, String url, String ssmId,
			String key, Object value) {
		Map<String, Object> params = IUtils.getRestParamMap(IConsts.PRM_MACHINE_ID, ssmId,
				IConsts.PRM_KEY, key, IConsts.PRM_CLASS, value.getClass().getName(),
				IConsts.PRM_VALUE, IUtils.getStringFromValue(value));
		IUtils.logger.info("Params: " + params);
		return IUtils.sendPostRestRequest(rest, url + IConsts.API_ADD_EXT_VAR, null,
				Boolean.class, params, MediaType.APPLICATION_FORM_URLENCODED);
	}

}
