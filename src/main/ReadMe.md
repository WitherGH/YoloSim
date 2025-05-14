Trading Simulator Application Description - YOLOSIM

The Trading Simulator is a JavaFX-based application designed to simulate stock trading activities. It allows users to create simulated traders, buy and sell financial instruments, track portfolios, and visualize market data through charts. The application maintains persistence by saving the state of traders, instruments, and trades between sessions.

The application follows a Model-View-Controller (MVC) architecture with:
- **Model**: Contains data classes like Trader, Trade, Instrument
- **Service**: Manages business logic and data operations
- **UI**: Handles user interface components and controllers
- **Persistence**: Saves and loads application state

The simulator uses real historical stock data (when available) and simulated data through random walks when historical data isn't available.

## Key Functions and Description

### Model Components

1. **Trader**: Represents a trader with a portfolio, budget, and trade history
   - Manages budget for buying instruments
   - Tracks trade history using a custom linked list
   - Calculates total value (budget + portfolio value)

2. **Instrument**: Represents a financial instrument like stocks
   - Contains properties like symbol, name, price, type
   - Tracks price changes, percent changes, and market cap
   - Types include stocks and potentially crypto

3. **Trade**: Represents a trade transaction
   - Contains details like trader, instrument, type (BUY/SELL), quantity, and price
   - Records timestamp of trades

4. **Portfolio**: Contains holdings of financial instruments
   - Manages a collection of holdings
   - Calculates total portfolio balance

5. **Holding**: Represents ownership of a specific instrument quantity
   - Associates an instrument with a quantity owned

### Service Components

6. **TraderService**: Singleton that manages traders and trades
   - Creates, deletes, and renames traders
   - Executes and deletes trades
   - Recalculates portfolios after trade modifications
   - Manages trader selection state

7. **InstrumentService**: Manages financial instruments
   - Provides access to available stocks
   - Retrieves instrument details by symbol

8. **MarketDataService**: Provides market data for instruments
   - Gets current price for an instrument (real or simulated)
   - Provides OHLC (Open-High-Low-Close) data for charting
   - Contains random walk simulation for prices

9. **StockDataService**: Loads and provides historical stock data
   - Retrieves historical price data from CSV files
   - Provides daily OHLC data for specific symbols

10. **PersistenceService**: Handles saving and loading application state
    - Saves trader, instrument, and trade data between sessions
    - Loads saved state when application restarts

### UI Components

11. **MainController**: Main application controller
    - Manages the overall application structure
    - Handles navigation between different views

12. **ProfileController**: Manages trader profile information
    - Shows trader details and portfolio information
    - Allows budget management

13. **InstrumentsController**: Manages instrument display and trading
    - Displays available instruments and their prices
    - Facilitates buying and selling instruments

14. **TradesController**: Manages trade history and operations
    - Shows trade history for selected trader
    - Allows filtering, sorting, and searching trades
    - Supports deleting trades

15. **ChartsController**: Handles charting and visualization
    - Displays price charts for selected instruments
    - Shows candlestick (OHLC) charts

16. **CandleStickChart**: Custom chart for OHLC visualization
    - Renders candlestick price data visualization
    - Displays price line and area fill
    - Customizable appearance

### Utility and Infrastructure

17. **UIThreadUtil**: Manages UI thread operations
    - Runs operations on UI thread or in the background
    - Provides a managed thread executor service

18. **SearchUtils**: Provides search functionality
    - Implements case-insensitive substring search

19. **SortUtils**: Provides sorting utilities
    - Handles sorting collections by different criteria

20. **CustomLinkedList**: Custom data structure implementation
    - Provides linkedlist functionality with iteration support
    - Used for storing trade history

21. **DataSnapshot**: Data container for persistence
    - Holds collections of traders, instruments, and trades
    - Used when saving/loading application state

The application's main workflow allows users to:
1. Create and select traders
2. View and manage available instruments
3. Execute buy/sell trades for instruments
4. View portfolio holdings and value
5. Track trade history
6. Visualize price data through charts

The system maintains persistence between sessions by saving the state of traders, instruments, and trades to disk.