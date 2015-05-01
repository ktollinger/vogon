/*
 * Vogon personal finance/expense analyzer.
 * Licensed under Apache license: http://www.apache.org/licenses/LICENSE-2.0
 * Author: Dmitry Zolotukhin <zlogic@gmail.com>
 */
package org.zlogic.vogon.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zlogic.vogon.data.FinanceAccount;
import org.zlogic.vogon.data.VogonUser;
import org.zlogic.vogon.data.interop.VogonExportException;
import org.zlogic.vogon.data.interop.VogonImportException;
import org.zlogic.vogon.data.interop.VogonImportLogicalException;
import org.zlogic.vogon.data.interop.XmlExporter;
import org.zlogic.vogon.data.interop.XmlImporter;
import org.zlogic.vogon.data.tools.DatabaseMaintenance;
import org.zlogic.vogon.web.data.AccountRepository;
import org.zlogic.vogon.web.data.TransactionRepository;
import org.zlogic.vogon.web.data.UserRepository;
import org.zlogic.vogon.web.security.VogonSecurityUser;

/**
 * Spring MVC controller for importing/exporting data
 *
 * @author Dmitry Zolotukhin [zlogic@gmail.com]
 */
@Controller
@Transactional
@RequestMapping(value = "/service")
public class DataController {

	/**
	 * The EntityManager instance
	 */
	@PersistenceContext
	private EntityManager em;
	/**
	 * The users repository
	 */
	@Autowired
	private UserRepository userRepository;
	/**
	 * The transactions repository
	 */
	@Autowired
	private TransactionRepository transactionRepository;
	/**
	 * The accounts repository
	 */
	@Autowired
	private AccountRepository accountRepository;

	/**
	 * Imports uploaded XML data
	 *
	 * @param data the file to import
	 * @param userPrincipal the authenticated user
	 * @return true of import succeeded
	 */
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces = "application/json", consumes = "multipart/form-data")
	public @ResponseBody
	Boolean importData(@RequestParam("file") MultipartFile data, @AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		try {
			XmlImporter importer = new XmlImporter(data.getInputStream());
			importer.importData(user, em);
		} catch (IOException | VogonImportException | VogonImportLogicalException ex) {
			throw new RuntimeException(ex);
		}
		return true;
	}

	/**
	 * Returns all data
	 *
	 * @param outputStream the stream for XML output
	 * @param userPrincipal the authenticated user
	 */
	@RequestMapping(value = "/export", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/xml")
	public HttpEntity<byte[]> exportData(@AuthenticationPrincipal VogonSecurityUser userPrincipal) throws RuntimeException {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			XmlExporter exporter = new XmlExporter(outStream);
			Sort accountSort = new Sort(new Sort.Order(Sort.Direction.ASC, "id"));//NOI18N
			Sort transactionSort = new Sort(new Sort.Order(Sort.Direction.ASC, "id"));//NOI18N
			exporter.exportData(user, accountRepository.findByOwner(user, accountSort), transactionRepository.findByOwner(user, transactionSort), null);

			String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()); //NOI18N

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_XML);
			headers.setContentLength(outStream.size());
			headers.setContentDispositionFormData("attachment", "vogon-" + date + ".xml"); //NOI18N

			return new HttpEntity<>(outStream.toByteArray(), headers);
		} catch (VogonExportException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Recalculates balance for all user's accounts
	 *
	 * @param userPrincipal the authenticated user
	 * @return true on success
	 */
	@RequestMapping(value = "/recalculateBalance", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Boolean recalculateBalance(@AuthenticationPrincipal VogonSecurityUser userPrincipal) {
		VogonUser user = userRepository.findByUsername(userPrincipal.getUsername());
		DatabaseMaintenance databaseMaintenance = new DatabaseMaintenance();
		for (FinanceAccount account : accountRepository.findByOwner(user))
			databaseMaintenance.refreshAccountBalance(account, em);
		return true;
	}

	/**
	 * Performs a DB cleanup operation
	 *
	 * @return true on success
	 */
	@RequestMapping(value = "/cleanup", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Boolean cleanup() {
		DatabaseMaintenance databaseMaintenance = new DatabaseMaintenance();
		databaseMaintenance.cleanup(em);//TODO: allows this action only for administrative users?
		return true;
	}
}
