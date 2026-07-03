
import logo from '../../assets/logo.png';

export const Footer = () => {
  return (
    <footer className="bg-[#333338] text-gray-400 py-10 mt-auto">
      <div className="max-w-7xl mx-auto px-4">
        
        <div className="flex flex-col md:flex-row items-center justify-between border-b border-gray-600 pb-8 mb-8 gap-6">
          <div className="flex items-center gap-3">
            <img src={logo} alt="ShowMantra Logo" className="h-10 w-10 object-cover rounded-xl shadow-sm opacity-90 hover:opacity-100 transition-opacity" />
            <span className="text-2xl font-bold tracking-tight text-white">Show<span className="text-red-500">Mantra</span></span>
          </div>
          <div className="flex gap-4">
            <a href="#" className="hover:text-white transition-colors">24/7 Customer Care</a>
            <span className="text-gray-600">|</span>
            <a href="#" className="hover:text-white transition-colors">Resend Booking Confirmation</a>
            <span className="text-gray-600">|</span>
            <a href="#" className="hover:text-white transition-colors">Subscribe to Newsletter</a>
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-8 text-sm">
          <div>
            <h4 className="text-gray-200 font-semibold mb-4">Movies Now Showing</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white transition-colors">Action Movies</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Comedy Movies</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Drama Movies</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-gray-200 font-semibold mb-4">Events</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white transition-colors">Comedy Shows</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Music Shows</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Workshops</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-gray-200 font-semibold mb-4">Help</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white transition-colors">About Us</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Contact Us</a></li>
              <li><a href="#" className="hover:text-white transition-colors">FAQs</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-gray-200 font-semibold mb-4">Legal</h4>
            <ul className="space-y-2">
              <li><a href="#" className="hover:text-white transition-colors">Terms of Service</a></li>
              <li><a href="#" className="hover:text-white transition-colors">Privacy Policy</a></li>
            </ul>
          </div>
        </div>

        <div className="text-center text-xs border-t border-gray-600 pt-8">
          <p>Copyright {new Date().getFullYear()} © ShowMantra Pvt. Ltd. All Rights Reserved.</p>
          <p className="mt-2 text-gray-500">The content and images used on this site are copyright protected and copyrights vests with the respective owners.</p>
        </div>
      </div>
    </footer>
  );
};
