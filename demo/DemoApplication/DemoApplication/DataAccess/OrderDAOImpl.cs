using System.Collections.Generic;

namespace Application.DataAccess
{
	/**
	 * ///<summary>
	 * ///��������DAO����
	 * ///</summary>
	 */
	public class OrderDAOImpl : OrderDAO
	{
		/**
		 * ///<summary>
		 * ///��������
		 * ///</summary>
		 */
		public IList<string[]> findOrder()
		{
			return null;
		}

        /// <summary>
        /// �����ǉ�
        /// </summary>
        /// <param name="orderData">�����f�[�^</param>
        public void addOrder(string[] orderData)
        {
            //���f���ɂ͑��݂��Ȃ����\�b�h�B
            //�������ꂽ�R�[�h�ɒǉ������B
            //�u���f���ւ̔��f�Ɏ��s�����v���Ƃ����O�ɏo�͂���邱�Ƃ��m�F����B
        }
	}

}

