package com.dandian.admissiondemo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "User")
public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9203715820163398998L;
	/**
	 * 
	 */
	
	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private String username;
	@DatabaseField
	private String name;
	@DatabaseField
	private String nickname;
	@DatabaseField
	private String department;
	@DatabaseField
	private String gender;
	@DatabaseField
	private String birthday;
	@DatabaseField
	private String phone;
	@DatabaseField
	private String email;
	@DatabaseField
	private String withClass;
	@DatabaseField
	private String withCourse;
	@DatabaseField
	private String companyName;
	@DatabaseField
	private String loginStatus;
	@DatabaseField
	private String loginTime;
	@DatabaseField
	private int isModify;
	@DatabaseField
	private String allowModifyField;
	@DatabaseField
	private String checkCode;
	@DatabaseField
	private String userImage;
	@DatabaseField
	private String virtualClass;
	@DatabaseField
	private String userNumber;
	@DatabaseField
	private String domain;
	@DatabaseField
	private String userType;
	@DatabaseField
	private String recentlyUsedEquipment;
	@DatabaseField
	private String iosDeviceToken;
	@DatabaseField
	private String certificationPath;
	@DatabaseField
	private String userRating;
	@DatabaseField
	private String mainRole;
	@DatabaseField
	private String secondaryRole;
	@DatabaseField
	private String sortNumber;
	@DatabaseField
	private String banLogin;
	@DatabaseField
	private String password;
	@DatabaseField
	private String sClass;
	@DatabaseField
	private String sPhone;
	@DatabaseField
	private String sDormitory;
	@DatabaseField
	private String sEmail;
	@DatabaseField
	private String sStatus;
	@DatabaseField
	private String pName;
	@DatabaseField
	private String pPhone;
	@DatabaseField
	private String homeAddress;
	@DatabaseField
	private String remark;
	@DatabaseField
	private String rootDomain;
	@DatabaseField
	private String company;
	@DatabaseField
	private String idCard;
	@DatabaseField
	private String latestAddress;
	

	public String getLatestAddress() {
		return latestAddress;
	}

	public void setLatestAddress(String latestAddress) {
		this.latestAddress = latestAddress;
	}

	// private List<Equipment> loginEquipments;
	@ForeignCollectionField
	/** 
	 * 这里需要注意的是：属性类型只能是ForeignCollection<T>或者Collection<T> 
	 * 如果需要懒加载（延迟加载）可以在@ForeignCollectionField加上参数eager=false 
	 * 这个属性也就说明一个用户对应着多个设备
	 */
	private Collection<Equipment> loginEquipments;

	public User() {
		userType="";
		userNumber="";
		username="";
		name="";
		userImage="";
		
	}

	public User(JSONObject jo) {
		
		id = jo.optString("编号");
		username = jo.optString("学号");
		name = jo.optString("姓名");
		sClass = jo.optString("班号");
		birthday = jo.optString("出生日期");
		gender = jo.optString("性别");
		sPhone = jo.optString("学生电话");
		sDormitory= jo.optString("家庭住址");
		homeAddress = jo.optString("家庭住址");
		idCard = jo.optString("身份证号");
		userNumber = jo.optString("用户唯一码");
		sortNumber=jo.optString("考生号");
		userType = jo.optString("用户类型");
		loginStatus= jo.optString("是否录取");
		loginTime= jo.optString("入学年份");
		sStatus= jo.optString("学生状态");
		userImage = jo.optString("照片");
		domain = jo.optString("专业名称");
		rootDomain = jo.optString("院系名称");
		userRating = jo.optString("收费标准");
		pName = jo.optString("父亲姓名");
		pPhone = jo.optString("父亲电话");
		department = jo.optString("来源地区");
		remark = jo.optString("入学方式");
		checkCode= jo.optString("用户较验码");
		
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public User(net.minidev.json.JSONObject jo) {
		
		id = String.valueOf(jo.get("编号"));
		username = String.valueOf(jo.get("学号"));
		name = String.valueOf(jo.get("姓名"));

		birthday = String.valueOf(jo.get("出生日期"));
		gender = String.valueOf(jo.get("性别"));
		
		
		isModify = Integer.parseInt(String.valueOf(jo.get("是否修改")==null?"0":jo.get("是否修改")));
		allowModifyField = String.valueOf(jo.get("允许用户修改自身字段列表"));
		checkCode = String.valueOf(jo.get("用户较验码"));
		userImage = String.valueOf(jo.get("用户头像"));
		virtualClass = String.valueOf(jo.get("虚拟班级"));
		userNumber = String.valueOf(jo.get("用户唯一码"));
		domain = String.valueOf(jo.get("域名"));
		banLogin = String.valueOf(jo.get("禁止登录"));
		sortNumber = String.valueOf(jo.get("排序号"));
		mainRole = String.valueOf(jo.get("主要角色"));
		secondaryRole = String.valueOf(jo.get("辅助角色"));
		userRating = String.valueOf(jo.get("用户评级"));
		certificationPath = String.valueOf(jo.get("认证路径"));
		iosDeviceToken = String.valueOf(jo.get("IosDeviceToken"));
		recentlyUsedEquipment = String.valueOf(jo.get("最近使用设备"));
		loginEquipments = new ArrayList<Equipment>();
		net.minidev.json.JSONObject jole = (net.minidev.json.JSONObject)jo.get("用户使用设备");
		if (jole != null) {
			Set<String> keyset=jole.keySet();
			Iterator<String> keys = keyset.iterator();
			while (keys.hasNext()) {
				String str = keys.next();
				Equipment eq = new Equipment((net.minidev.json.JSONObject)jole.get(str));
				eq.setUser(this);
				loginEquipments.add(eq);
			}
		}

		companyName = String.valueOf(jo.get("单位名称"));
		company = String.valueOf(jo.get("单位"));
		sClass = String.valueOf(jo.get("班级"));
		sPhone = String.valueOf(jo.get("学生电话"));
		sDormitory = String.valueOf(jo.get("学生宿舍"));
		sEmail = String.valueOf(jo.get("学生邮箱"));
		sStatus = String.valueOf(jo.get("学生状态"));
		pName = String.valueOf(jo.get("家长姓名"));
		pPhone = String.valueOf(jo.get("家长电话"));
		homeAddress = String.valueOf(jo.get("家庭住址"));
		remark = String.valueOf(jo.get("备注"));
		rootDomain = String.valueOf(jo.get("更域名"));
		
	}
	/**
	 * 编号
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 用户名
	 * 
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 姓名
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 昵称
	 * 
	 * @return
	 */
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * 部门
	 * 
	 * @return
	 */
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * 性别
	 * 
	 * @return
	 */
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * 出生日期
	 * 
	 * @return
	 */
	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	/**
	 * 手机
	 * 
	 * @return
	 */
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * 电邮
	 * 
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * 所带班级
	 * 
	 * @return
	 */
	public String getWithClass() {
		return withClass;
	}

	public void setWithClass(String withClass) {
		this.withClass = withClass;
	}

	/**
	 * 所带课程
	 * 
	 * @return
	 */
	public String getWithCourse() {
		return withCourse;
	}

	public void setWithCourse(String withCourse) {
		this.withCourse = withCourse;
	}

	/**
	 * 单位名称
	 * 
	 * @return
	 */
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	/**
	 * 登录状态
	 * 
	 * @return
	 */
	public String getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	/**
	 * 登录时间
	 * 
	 * @return
	 */
	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	/**
	 * 是否修改
	 * 
	 * @return
	 */
	public int getIsModify() {
		return isModify;
	}

	public void setIsModify(int isModify) {
		this.isModify = isModify;
	}

	/**
	 * 允许修改字段
	 * 
	 * @return
	 */
	public String getAllowModifyField() {
		return allowModifyField;
	}

	public void setAllowModifyField(String allowModifyField) {
		this.allowModifyField = allowModifyField;
	}

	/**
	 * 用户校验码
	 * 
	 * @return
	 */
	public String getCheckCode() {
		return checkCode;
	}

	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}

	/**
	 * 用户头像
	 * 
	 * @return
	 */
	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	/**
	 * 虚拟班级
	 * 
	 * @return
	 */
	public String getVirtualClass() {
		return virtualClass;
	}

	public void setVirtualClass(String virtualClass) {
		this.virtualClass = virtualClass;
	}

	/**
	 * 用户唯一码
	 * 
	 * @return
	 */
	public String getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	/**
	 * 域名
	 * 
	 * @return
	 */
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * 用户类型
	 * 
	 * @return
	 */
	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	/**
	 * 用户使用设备
	 * 
	 * @return
	 */
	public Collection<Equipment> getLoginEquipments() {
		return loginEquipments;
	}

	public void setLoginEquipments(List<Equipment> loginEquipments) {
		this.loginEquipments = loginEquipments;
	}

	/**
	 * 用户最经使用设备
	 * 
	 * @return
	 */
	public String getRecentlyUsedEquipment() {
		return recentlyUsedEquipment;
	}

	public void setRecentlyUsedEquipment(String recentlyUsedEquipment) {
		this.recentlyUsedEquipment = recentlyUsedEquipment;
	}

	/**
	 * 功能描述:
	 * 
	 * @return
	 */
	public String getIosDeviceToken() {
		return iosDeviceToken;
	}

	public void setIosDeviceToken(String iosDeviceToken) {
		this.iosDeviceToken = iosDeviceToken;
	}

	/**
	 * 认证路径
	 * 
	 * @return
	 */
	public String getCertificationPath() {
		return certificationPath;
	}

	public void setCertificationPath(String certificationPath) {
		this.certificationPath = certificationPath;
	}

	/**
	 * 用户评级
	 * 
	 * @return
	 */
	public String getUserRating() {
		return userRating;
	}

	public void setUserRating(String userRating) {
		this.userRating = userRating;
	}

	/**
	 * 主要角色
	 * 
	 * @return
	 */
	public String getMainRole() {
		return mainRole;
	}

	public void setMainRole(String mainRole) {
		this.mainRole = mainRole;
	}

	/**
	 * 辅助角色
	 * 
	 * @return
	 */
	public String getSecondaryRole() {
		return secondaryRole;
	}

	public void setSecondaryRole(String secondaryRole) {
		this.secondaryRole = secondaryRole;
	}

	/**
	 * 排序号
	 * 
	 * @return
	 */
	public String getSortNumber() {
		return sortNumber;
	}

	public void setSortNumber(String sortNumber) {
		this.sortNumber = sortNumber;
	}

	/**
	 * 禁止登录
	 * 
	 * @return
	 */
	public String getBanLogin() {
		return banLogin;
	}

	public void setBanLogin(String banLogin) {
		this.banLogin = banLogin;
	}

	/**
	 * 密码
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 学生班级
	 * 
	 * @return
	 */
	public String getsClass() {
		return sClass;
	}

	public void setsClass(String sClass) {
		this.sClass = sClass;
	}

	/**
	 * 学生电话
	 * 
	 * @return
	 */
	public String getsPhone() {
		return sPhone;
	}

	public void setsPhone(String sPhone) {
		this.sPhone = sPhone;
	}

	/**
	 * 学生宿舍
	 * 
	 * @return
	 */
	public String getsDormitory() {
		return sDormitory;
	}

	public void setsDormitory(String sDormitory) {
		this.sDormitory = sDormitory;
	}

	/**
	 * 学生邮箱
	 * 
	 * @return
	 */
	public String getsEmail() {
		return sEmail;
	}

	public void setsEmail(String sEmail) {
		this.sEmail = sEmail;
	}

	/**
	 * 学生状态
	 * 
	 * @return
	 */
	public String getsStatus() {
		return sStatus;
	}

	public void setsStatus(String sStatus) {
		this.sStatus = sStatus;
	}

	/**
	 * 家长姓名
	 * 
	 * @return
	 */
	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	/**
	 * 家长电话
	 * 
	 * @return
	 */
	public String getpPhone() {
		return pPhone;
	}

	public void setpPhone(String pPhone) {
		this.pPhone = pPhone;
	}

	/**
	 * 家庭住址
	 * 
	 * @return
	 */
	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

	/**
	 * 备注
	 * 
	 * @return
	 */
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * 根域名
	 * 
	 * @return
	 */
	public String getRootDomain() {
		return rootDomain;
	}

	public void setRootDomain(String rootDomain) {
		this.rootDomain = rootDomain;
	}

	/**
	 * 单位
	 * 
	 * @return
	 */
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
