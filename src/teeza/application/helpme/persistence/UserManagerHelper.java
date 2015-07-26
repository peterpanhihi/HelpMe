package teeza.application.helpme.persistence;

import teeza.application.helpme.model.User;

public interface UserManagerHelper {

	public static final String DATABASE_NAME = "HelpMe_login";
	public static final int DATABASE_VERSION = 1;

	/**
	 * �ӡ��૿������ User ŧ�ҹ������
	 * 
	 * @param user
	 * @return �ҡ�ѹ�֡����稨��觤�� row ID ��Ѻ�� ����� error ���� -1
	 */
	public long registerUser(User user);

	/**
	 * �ӡ���� User �����ͤ�Թ���� username ��� password <br />
	 * �١��ͧ�ç�Ѻ㹰ҹ������������� (�ѹ���͡�� query sqlite ����ͧ) <br />
	 * �ҡ query ���� username, password �����բ����� �ʴ���� ��ͤ�Թ�١��ͧ
	 * 
	 * @param user
	 * @return - �ҡ�ç ���觤���� user �����Ѻ� �ҡ���ç���� null
	 */
	public User checkUserLogin(User user);

	/**
	 * ����Ѻ����¹ password �·ӡ�� query �Ң����� username, password ��͹ <br />
	 * �ҡ��鹶֧ update ������¹ password ����᷹
	 * 
	 * @param user
	 * @return - �觤�� �ӹǹ�Ƿ���ա�� update
	 */
	public int changePassword(User user);

}
