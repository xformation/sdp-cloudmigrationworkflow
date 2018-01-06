/**
 * 
 */
package com.synectiks.services.migration.controllers;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.synectiks.commons.constants.IConsts;
import com.synectiks.commons.interfaces.IApiController;
import com.synectiks.commons.utils.IUtils;
import com.synectiks.services.migration.services.MigrationWorkflowService;

/**
 * @author Rajesh
 */
@Controller
@RequestMapping(path = IApiController.API_PATH
		+ IApiController.URL_MIGRATION, method = RequestMethod.POST)
@CrossOrigin
public class MigrationController {

	private static final Logger logger = LoggerFactory
			.getLogger(MigrationController.class);

	private static final String tmpDir = "/tmp";

	@Autowired
	private MigrationWorkflowService service;

	@RequestMapping(path = "/createWorkflow")
	public ResponseEntity<Object> createMigrationWorkflow(
			@RequestParam(name = IConsts.PRM_MACHINE_ID) String id,
			HttpServletRequest request) {
		String res = null;
		try {
			res = service.createMigrationWorkflow(id, request);
			logger.info("Machine ID: " + res);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(IUtils.getFailedResponse(th.getMessage()));
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(res);
	}

	@RequestMapping(path = "/updateWorkflow")
	public ResponseEntity<Object> updateWorkflow(@RequestParam String ssmId,
			@RequestParam String event, @RequestParam(defaultValue = "{}") String json,
			@RequestParam(required = false) MultipartFile[] files,
			HttpServletRequest request) {
		Object res = null;
		String tmpPath = null;
		try {
			JSONObject data = new JSONObject(json);
			tmpPath = IUtils.getTempFileSavePath(IUtils.getUserFromRequest(request),
					tmpDir);
			res = service.updateWorkflow(request, ssmId, event, data, files, tmpPath);
		} catch (Throwable th) {
			logger.error(th.getMessage(), th);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
					.body(IUtils.getFailedResponse(th.getMessage()));
		} finally {
			try {
				IUtils.deleteFiles(tmpPath);
			} catch (Exception ex) {
			}
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(res);
	}

}
