using System.Collections.Generic;

namespace Application.Business
{
	/**
	 * ///<summary>
	 * ///ビジネス層の窓口
	 * ///</summary>
	 * 
	 */
	public class BusinessFacade
	{
		/**
		 * ///<summary>
		 * ///注文検索
		 * ///</summary>
		 */
		public IList<Order> findOrder()
		{
			return null;
		}

        /// <summary>
        /// モデルには存在しないメソッド
        /// </summary>
        /// <param name="orders"></param>
        public void addOrder(IList<Order> orders)
        {
        }

	}

}

