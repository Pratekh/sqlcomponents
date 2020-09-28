package com.techatpark.scubebird.core.specification.java;

import com.techatpark.scubebird.core.model.*;
import com.techatpark.scubebird.core.specification.Specification;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaSpecification extends Specification {

	@Override
	public void writeSpecification(DaoProject project) {
		ORM orm = project.getOrm();
		ProcessedEntity processedEntity = new ProcessedEntity(orm);
		for (Entity entity : orm.getEntities()) {
			try {
				processedEntity.setEntity(entity);
				writeBeanSpecification(processedEntity, project.getSrcFolder());
				writeDaoSpecification(processedEntity, project.getSrcFolder(),project.getDaoSuffix());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Service service : orm.getServices()) {
			try {
				writeServiceSpecification(service, project.getSrcFolder(),project.getDaoSuffix());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TemplateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void writeBeanSpecification(ProcessedEntity entity, String srcFolder)
			throws IOException, TemplateException {
		String packageFolder = getPackageAsFolder(srcFolder, entity
				.getBeanPackage());
		new File(packageFolder).mkdirs();
		processTemplates(entity, packageFolder + File.separator
				+ entity.getName() + ".java", beanTemplate);
	}

	private void writeDaoSpecification(ProcessedEntity entity, String srcFolder,String daoSuffix)
			throws IOException, TemplateException {
		String packageFolder = getPackageAsFolder(srcFolder, entity
				.getDaoPackage());
		new File(packageFolder).mkdirs();
		processTemplates(entity, packageFolder + File.separator
				+ entity.getName() + "Dao"  + daoSuffix.trim() + ".java", daoTemplate);
	}

	private void writeServiceSpecification(Service service, String srcFolder,String daoSuffix)
			throws IOException, TemplateException {
		String packageFolder = getPackageAsFolder(srcFolder, service
				.getDaoPackage());
		new File(packageFolder).mkdirs();
		processTemplates(service, packageFolder + File.separator
				+ service.getServiceName() + "Dao"  + daoSuffix.trim() + ".java", serviceTemplate);
	}

	private void processTemplates(Object model, String targetFile,
			Template template) throws IOException, TemplateException {
		template.process(model, new FileWriter(targetFile));
	}

	public JavaSpecification() {
		freeMarkerConfiguration = new Configuration();
		freeMarkerConfiguration.setClassForTemplateLoading(
				JavaSpecification.class, "/template/dao/java");

		try {
			beanTemplate = freeMarkerConfiguration.getTemplate("bean.ftl");
			daoTemplate = freeMarkerConfiguration.getTemplate("dao.ftl");
			serviceTemplate = freeMarkerConfiguration
					.getTemplate("service.ftl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private final Configuration freeMarkerConfiguration;
	private Template daoTemplate;
	private Template beanTemplate;
	private Template serviceTemplate;

	public static class ProcessedForignKey {
		private ProcessedEntity processedEntity;
		private List<Property> properties;

		public ProcessedEntity getProcessedEntity() {
			return processedEntity;
		}

		public void setProcessedEntity(ProcessedEntity processedEntity) {
			this.processedEntity = processedEntity;
		}

		public List<Property> getProperties() {
			return properties;
		}

		public void setProperties(List<Property> properties) {
			this.properties = properties;
		}
	}

	public static class ProcessedEntity {

		private Entity entity;

		private List<ProcessedForignKey> forignKeys;

		public List<ProcessedForignKey> getForignKeys() {
			return forignKeys;
		}

		public void setForignKeys(List<ProcessedForignKey> forignKeys) {
			this.forignKeys = forignKeys;
		}

		public List<String> getUniqueConstraintGroupNames() {
			List<String> uniqueConstraintGroupNames = new ArrayList<String>();
			String prevUniqueConstraintGroupName = null;
			String uniqueConstraintGroupName = null;
			for (Property property : getProperties()) {
				uniqueConstraintGroupName = property.getUniqueConstraintGroup();
				if (uniqueConstraintGroupName != null
						&& !uniqueConstraintGroupName
								.equals(prevUniqueConstraintGroupName)) {
					uniqueConstraintGroupNames.add(uniqueConstraintGroupName);
					prevUniqueConstraintGroupName = uniqueConstraintGroupName;
				}
			}
			return uniqueConstraintGroupNames;
		}

		public int getHighestPKIndex() {
			int highestPKIndex = 0;
			for (Property property : getProperties()) {
				if (highestPKIndex < property.getPrimaryKeyIndex()) {
					highestPKIndex = property.getPrimaryKeyIndex();
				}
			}
			return highestPKIndex;
		}

		private ORM orm;

		public ProcessedEntity(ORM orm) {
			setOrm(orm);

		}

		private void configForignKeys() {
			List<ProcessedForignKey> forignKeys = new ArrayList<ProcessedForignKey>();
			List<String> uniqueForignTableNames = new ArrayList<String>();

			for (Property property : getProperties()) {
				for (ForeignKey foreignKey : property.getColumn()
						.getForeignKeys()) {
					if (!uniqueForignTableNames.contains(foreignKey
							.getTableName())) {
						uniqueForignTableNames.add(foreignKey.getTableName());
					}
				}
			}

			for (String tableName : uniqueForignTableNames) {
				forignKeys.add(getProcessedForignKey(tableName));
			}
			setForignKeys(forignKeys);
		}

		private ProcessedForignKey getProcessedForignKey(String tableName) {
			ProcessedForignKey processedForignKey = new ProcessedForignKey();
			processedForignKey.setProcessedEntity(new ProcessedEntity(orm));
			List<Property> propertiesList = new ArrayList<Property>();
			for (Property property : getProperties()) {
				for (ForeignKey foreignKey : property.getColumn()
						.getForeignKeys()) {
					if (foreignKey.getTableName().equals(tableName)) {
						if (!propertiesList.contains(property)) {
							propertiesList.add(property);
						}
					}
				}
			}
			return processedForignKey;
		}

		public ORM getOrm() {
			return orm;
		}

		private void setOrm(ORM orm) {
			this.orm = orm;
			//configForignKeys();
		}

		public Entity getEntity() {
			return entity;
		}

		public void setEntity(Entity entity) {
			this.entity = entity;
		}

		public String getBeanPackage() {
			return entity.getBeanPackage();
		}

		public String getCategoryName() {
			return entity.getCategoryName();
		}

		public String getDaoPackage() {
			return entity.getDaoPackage();
		}

		public String getName() {
			return entity.getName();
		}

		public String getPluralName() {
			return entity.getPluralName();
		}

		public List<Property> getProperties() {
			return entity.getProperties();
		}

		public String getRemarks() {
			return entity.getRemarks();
		}

		public String getSchemaName() {
			return entity.getSchemaName();
		}

		public Table getTable() {
			return entity.getTable();
		}

		public String getTableName() {
			return entity.getTableName();
		}

		public String getTableType() {
			return entity.getTableType();
		}

		public void setBeanPackage(String beanPackage) {
			entity.setBeanPackage(beanPackage);
		}

		public void setCategoryName(String categoryName) {
			entity.setCategoryName(categoryName);
		}

		public void setDaoPackage(String daoPackage) {
			entity.setDaoPackage(daoPackage);
		}

		public void setName(String name) {
			entity.setName(name);
		}

		public void setPluralName(String pluralName) {
			entity.setPluralName(pluralName);
		}

		public void setProperties(List<Property> properties) {
			entity.setProperties(properties);
		}

		public void setRemarks(String remarks) {
			entity.setRemarks(remarks);
		}

		public void setSchemaName(String schemaName) {
			entity.setSchemaName(schemaName);
		}

		public void setTable(Table table) {
			entity.setTable(table);
		}

		public void setTableName(String tableName) {
			entity.setTableName(tableName);
		}

		public void setTableType(String type) {
			entity.setTableType(type);
		}

		public String getSequenceName() {
			return entity.getSequenceName();
		}

		public void setSequenceName(String sequenceName) {
			entity.setSequenceName(sequenceName);
		}

	}

}
